/**
 * DC5-control Cloudflare Worker
 * Backend API con Supabase
 *
 * Rutas:
 *   POST /api/email/send   → Envía el correo de confirmación de cuenta (Resend). PÚBLICA.
 *   POST /api/email/verify → Valida el token del correo y activa la cuenta.  PÚBLICA.
 *   /api/* (resto)         → Proxy Supabase con SERVICE_ROLE. CERRADO (requiere x-api-key).
 */

const corsHeaders = {
  "Access-Control-Allow-Origin": "*",
  "Access-Control-Allow-Methods": "GET, POST, PUT, DELETE, OPTIONS",
  "Access-Control-Allow-Headers": "Content-Type, Authorization, apikey, x-api-key",
};

async function sbFetch(table, method, body, queryParams, env, isStorage = false) {
  const supabaseUrl = env.SUPABASE_URL;
  const supabaseKey = env.SUPABASE_SERVICE_ROLE_KEY;

  const headers = {
    "apikey": supabaseKey,
    "Authorization": `Bearer ${supabaseKey}`,
  };

  if (!isStorage) {
    headers["Content-Type"] = "application/json";
  }

  let url = isStorage
    ? `${supabaseUrl}/storage/v1/object/${table}`
    : `${supabaseUrl}/rest/v1/${table}${queryParams}`;

  const opts = { method, headers };

  if (method === "POST" || method === "PUT") {
    if (!isStorage) {
      opts.headers["Prefer"] = "return=representation";
      opts.body = JSON.stringify(body);
    } else {
      opts.body = body; // Binary body
    }
  }

  const res = await fetch(url, opts);
  if (res.ok) {
    return await res.json();
  } else {
    const error = await res.text();
    throw new Error(error);
  }
}

// ═══════════════════════════════════════════════════════════════════
// Confirmación de correo propia (Resend) — sin depender del mailer
// de Supabase (limitado en el plan gratuito).
// ═══════════════════════════════════════════════════════════════════

// Rate limit en memoria (mejor esfuerzo; el aislamiento por email y
// la verificación de cuenta existente son la barrera principal).
const EMAIL_RATE = new Map();

function rateLimited(key, max, windowMs) {
  const now = Date.now();
  const arr = (EMAIL_RATE.get(key) || []).filter((t) => now - t < windowMs);
  if (arr.length >= max) {
    EMAIL_RATE.set(key, arr);
    return true;
  }
  arr.push(now);
  EMAIL_RATE.set(key, arr);
  return false;
}

function json(obj, status) {
  return new Response(JSON.stringify(obj), {
    status,
    headers: { ...corsHeaders, "Content-Type": "application/json" },
  });
}

function b64url(str) {
  return btoa(unescape(encodeURIComponent(str)))
    .replace(/\+/g, "-")
    .replace(/\//g, "_")
    .replace(/=+$/, "");
}

function b64urlDecode(str) {
  try {
    const b64 = str.replace(/-/g, "+").replace(/_/g, "/");
    return decodeURIComponent(escape(atob(b64)));
  } catch (e) {
    return null; // token corrupto
  }
}

async function hmacSign(payload, secret) {
  const enc = new TextEncoder();
  const key = await crypto.subtle.importKey(
    "raw", enc.encode(secret), { name: "HMAC", hash: "SHA-256" }, false, ["sign"]
  );
  const sig = await crypto.subtle.sign("HMAC", key, enc.encode(payload));
  return [...new Uint8Array(sig)]
    .map((b) => b.toString(16).padStart(2, "0"))
    .join("");
}

function safeEqual(a, b) {
  if (a.length !== b.length) return false;
  let out = 0;
  for (let i = 0; i < a.length; i++) out |= a.charCodeAt(i) ^ b.charCodeAt(i);
  return out === 0;
}

function confirmEmailHtml(link) {
  return `<!DOCTYPE html>
<html><body style="margin:0;padding:0;background:#f4f5f7;font-family:Arial,Helvetica,sans-serif;">
  <table role="presentation" width="100%" cellpadding="0" cellspacing="0" style="padding:32px 16px;">
    <tr><td align="center">
      <table role="presentation" width="480" cellpadding="0" cellspacing="0" style="background:#ffffff;border-radius:10px;padding:40px 36px;">
        <tr><td align="center" style="padding-bottom:8px;">
          <div style="font-size:22px;font-weight:bold;color:#1e3a5f;letter-spacing:1px;">ACE CONTROL</div>
          <div style="font-size:11px;color:#8b95a3;letter-spacing:2px;margin-top:2px;">GENERADOR DE CONSTANCIAS DC-3</div>
        </td></tr>
        <tr><td style="padding-top:24px;">
          <p style="font-size:16px;color:#1f2937;margin:0 0 12px;">Confirma tu cuenta</p>
          <p style="font-size:14px;color:#4b5563;line-height:1.6;margin:0 0 28px;">
            Gracias por registrarte. Para activar tu acceso y comenzar a llenar tus datos como agente capacitador,
            haz clic en el siguiente botón:
          </p>
        </td></tr>
        <tr><td align="center" style="padding-bottom:28px;">
          <a href="${link}" style="background:#1e3a5f;color:#ffffff;text-decoration:none;font-size:15px;font-weight:bold;padding:14px 34px;border-radius:6px;display:inline-block;">Confirmar mi cuenta</a>
        </td></tr>
        <tr><td>
          <p style="font-size:12px;color:#9aa4b2;line-height:1.6;margin:0;">
            Si no funciona el botón, copia y pega este enlace en tu navegador:<br>
            <a href="${link}" style="color:#1e3a5f;word-break:break-all;">${link}</a>
          </p>
          <p style="font-size:12px;color:#9aa4b2;margin:20px 0 0;">
            El enlace vence en 24 horas. Si no solicitaste esta cuenta, puedes ignorar este correo.
          </p>
        </td></tr>
      </table>
      <p style="font-size:11px;color:#b0b7c3;margin-top:20px;">© ACE Control · EHS Solutions</p>
    </td></tr>
  </table>
</body></html>`;
}

// Normaliza el remitente: repara "Name <correo" sin ">" de cierre.
function normalizeFrom(raw, fallback) {
  const v = (raw || "").trim();
  if (!v) return fallback;
  if (v.includes("<") && !v.includes(">")) return v + ">";
  return v;
}

async function handleSendConfirm(request, env, url) {
  try {
    if (!env.API_KEY) return json({ error: "API_KEY no configurada en Cloudflare." }, 403);

    const ip = request.headers.get("cf-connecting-ip") || "unknown";
    if (rateLimited("ip:" + ip, 5, 3600000)) {
      return json({ error: "Demasiadas solicitudes. Espera una hora e reintenta." }, 429);
    }

    const body = await request.json();
    const to = String((body && body.to) || "").trim().toLowerCase();
    if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(to)) return json({ error: "Correo inválido." }, 400);

    if (rateLimited("mail:" + to, 3, 3600000)) {
      return json({ error: "Ya enviamos varios correos a esta dirección. Revisa tu bandeja (y spam) o reintenta más tarde." }, 429);
    }

    const sbHeaders = {
      "apikey": env.SUPABASE_SERVICE_ROLE_KEY,
      "Authorization": "Bearer " + env.SUPABASE_SERVICE_ROLE_KEY,
      "Content-Type": "application/json",
    };

    // Anti-spam: solo enviamos a correos con cuenta real en Supabase Auth
    const authRes = await fetch(
      `${env.SUPABASE_URL}/auth/v1/admin/users?email=${encodeURIComponent(to)}&per_page=1`,
      { headers: sbHeaders }
    );
    if (authRes.ok) {
      const authData = await authRes.json();
      if (!authData.users || authData.users.length === 0) {
        return json({ error: "No existe una cuenta con ese correo." }, 404);
      }
    }

    // Solo si el perfil existe, está activo y aún no confirma
    const profileRes = await fetch(
      `${env.SUPABASE_URL}/rest/v1/app_users?email=eq.${encodeURIComponent(to)}&select=approved,email_confirmed`,
      { headers: sbHeaders }
    );
    const profiles = profileRes.ok ? await profileRes.json() : [];
    if (!profiles || profiles.length === 0) return json({ error: "La cuenta no tiene perfil de acceso." }, 404);
    if (profiles[0].approved === false) return json({ error: "La cuenta está suspendida." }, 403);
    if (profiles[0].email_confirmed === true) return json({ ok: true, already: true });

    // Sin API key de Resend: no romper el flujo (como en EHS-SOLUTIONS)
    if (!env.RESEND_API_KEY) {
      return json({ skipped: true, reason: "RESEND_API_KEY no configurada en Cloudflare." }, 200);
    }

    // Token firmado con HMAC-SHA256 (vence en 24 h)
    const exp = Date.now() + 24 * 60 * 60 * 1000;
    const payload = `${to}|${exp}`;
    const token = b64url(payload) + "." + (await hmacSign(payload, env.API_KEY));
    const link = `${url.origin}/app.html?confirm=${encodeURIComponent(token)}`;

    const from = normalizeFrom(env.RESEND_FROM_EMAIL, "ACE Control <notificaciones@ehs-solutions.online>");
    const res = await fetch("https://api.resend.com/emails", {
      method: "POST",
      headers: { "Authorization": "Bearer " + env.RESEND_API_KEY, "Content-Type": "application/json" },
      body: JSON.stringify({
        from,
        to: [to],
        subject: "Confirma tu cuenta en ACE Control",
        html: confirmEmailHtml(link),
      }),
    });
    if (!res.ok) {
      const err = await res.text();
      return json({ error: "No se pudo enviar el correo.", detail: err }, 500);
    }
    return json({ sent: true });
  } catch (e) {
    return json({ error: e.message }, 500);
  }
}

async function handleVerifyConfirm(request, env) {
  try {
    if (!env.API_KEY) return json({ error: "API_KEY no configurada en Cloudflare." }, 403);

    const body = await request.json();
    const token = String((body && body.token) || "");
    const parts = token.split(".");
    if (parts.length !== 2) return json({ ok: false, error: "Token inválido." }, 400);

    const payload = b64urlDecode(parts[0]);
    if (payload === null) return json({ ok: false, error: "Token inválido." }, 400);
    const expected = await hmacSign(payload, env.API_KEY);
    if (!safeEqual(parts[1], expected)) return json({ ok: false, error: "Token inválido." }, 400);

    const idx = payload.lastIndexOf("|");
    const email = payload.slice(0, idx);
    const expStr = payload.slice(idx + 1);
    if (!email || !expStr || Date.now() > Number(expStr)) {
      return json({ ok: false, error: "El enlace expiró. Inicia sesión para reenviar el correo." }, 400);
    }

    const res = await fetch(
      `${env.SUPABASE_URL}/rest/v1/app_users?email=eq.${encodeURIComponent(email)}`,
      {
        method: "PATCH",
        headers: {
          "apikey": env.SUPABASE_SERVICE_ROLE_KEY,
          "Authorization": "Bearer " + env.SUPABASE_SERVICE_ROLE_KEY,
          "Content-Type": "application/json",
          "Prefer": "return=representation",
        },
        body: JSON.stringify({ email_confirmed: true }),
      }
    );
    if (!res.ok) {
      const err = await res.text();
      return json({ ok: false, error: "No se pudo confirmar la cuenta.", detail: err }, 500);
    }
    return json({ ok: true });
  } catch (e) {
    return json({ ok: false, error: e.message }, 500);
  }
}

// ═══════════════════════════════════════════════════════════════════
// Pagos — Mercado Pago (Checkout Pro)
// ═══════════════════════════════════════════════════════════════════

// El token de Mercado Pago puede estar guardado en Cloudflare como
// MP_ACCESS_TOKEN, MERCADOPAGO_ACCESS_TOKEN o MERCADOPAGO_ACCESS_TOKEN_2
// (todos los nombres validos — evita despliegues fallidos por el nombre).
function getMpToken(env) {
  return env.MP_ACCESS_TOKEN || env.MERCADOPAGO_ACCESS_TOKEN || env.MERCADOPAGO_ACCESS_TOKEN_2 || null;
}

async function handlePaymentCreate(request, env, url) {
  try {
    if (!env.API_KEY) return json({ error: "API_KEY no configurada en Cloudflare." }, 403);
    if (!getMpToken(env)) return json({ error: "MP_ACCESS_TOKEN no configurada en Cloudflare." }, 503);

    const ip = request.headers.get("cf-connecting-ip") || "unknown";
    if (rateLimited("pay:" + ip, 10, 3600000)) {
      return json({ error: "Demasiadas solicitudes. Inténtalo más tarde." }, 429);
    }

    // 1) Identidad: verificar el token de sesión del agente en Supabase
    const bearer = (request.headers.get("Authorization") || "").replace(/^Bearer\s+/i, "").trim();
    if (!bearer) return json({ error: "No autenticado." }, 401);

    const userRes = await fetch(`${env.SUPABASE_URL}/auth/v1/user`, {
      headers: { "apikey": env.SUPABASE_SERVICE_ROLE_KEY, "Authorization": "Bearer " + bearer },
    });
    if (!userRes.ok) return json({ error: "Sesión inválida. Inicia sesión de nuevo." }, 401);
    const user = await userRes.json();
    const email = String((user && user.email) || "").toLowerCase();
    if (!email) return json({ error: "Sesión inválida." }, 401);

    const sbHeaders = {
      "apikey": env.SUPABASE_SERVICE_ROLE_KEY,
      "Authorization": "Bearer " + env.SUPABASE_SERVICE_ROLE_KEY,
      "Content-Type": "application/json",
    };

    // 2) La cuenta debe estar activa y confirmada
    const profRes = await fetch(
      `${env.SUPABASE_URL}/rest/v1/app_users?email=eq.${encodeURIComponent(email)}&select=approved,email_confirmed`,
      { headers: sbHeaders }
    );
    const prof = profRes.ok ? (await profRes.json())[0] : null;
    if (!prof || prof.approved === false || prof.email_confirmed === false) {
      return json({ error: "Tu cuenta no está activa." }, 403);
    }

    // 3) Paquete solicitado (precio oficial de la BD, nunca del cliente)
    const body = await request.json();
    const packId = String((body && body.pack) || "");
    const packRes = await fetch(
      `${env.SUPABASE_URL}/rest/v1/credit_packages?id=eq.${encodeURIComponent(packId)}&active=eq.true&select=*`,
      { headers: sbHeaders }
    );
    const packs = packRes.ok ? await packRes.json() : [];
    if (!packs || packs.length === 0) return json({ error: "Paquete no disponible." }, 404);
    const pack = packs[0];

    // 4) Registrar la orden como pendiente
    const orderId = crypto.randomUUID();
    const insRes = await fetch(`${env.SUPABASE_URL}/rest/v1/payment_orders`, {
      method: "POST",
      headers: { ...sbHeaders, "Prefer": "return=minimal" },
      body: JSON.stringify({
        id: orderId,
        agent_email: email,
        pack_id: pack.id,
        amount: Number(pack.price),
        currency: "MXN",
        status: "pending",
      }),
    });
    if (!insRes.ok) return json({ error: "No se pudo registrar la orden." }, 500);

    // 5) Crear el checkout de Mercado Pago — Checkout Pro vía ORDERS API
    // (la API de Preferences está en proceso de descontinuación; MP la
    // reemplaza por /v1/orders). El precio (unit_price/total_amount)
    // sale siempre de `pack`, leído de la BD en el paso 3 — nunca del
    // cliente. La notificación de pago se recibe en la URL configurada
    // a nivel de aplicación en el dashboard de MP (Webhooks →
    // "Order (Mercado Pago)"), no en este request.
    const priceStr = Number(pack.price).toFixed(2);
    const orderRes = await fetch("https://api.mercadopago.com/v1/orders", {
      method: "POST",
      headers: {
        "Authorization": "Bearer " + getMpToken(env),
        "Content-Type": "application/json",
        "X-Idempotency-Key": orderId,
      },
      body: JSON.stringify({
        type: "online",
        processing_mode: "manual",
        external_reference: orderId,
        total_amount: priceStr,
        description: "ACE Control — " + pack.name,
        items: [{
          // OJO: la API de Orders rechaza "id" dentro de items (error
          // unsupported_properties). Verificado en vivo 2026-09-16.
          title: "ACE Control — " + pack.name + " (" + pack.credits + " documentos)",
          unit_price: priceStr,
          quantity: 1,
        }],
        config: {
          online: {
            success_url: `${url.origin}/app.html?compra=ok&order=${orderId}`,
            pending_url: `${url.origin}/app.html?compra=pending`,
            failure_url: `${url.origin}/app.html?compra=fail`,
            auto_return: "approved",
          },
        },
      }),
    });
    const order = await orderRes.json().catch(() => ({}));
    if (!orderRes.ok || !order || !order.id) {
      return json({ error: "No se pudo iniciar el pago con Mercado Pago.", detail: (order && (order.message || JSON.stringify(order.cause))) || "" }, 500);
    }
    if (!order.checkout_url) {
      return json({ error: "Mercado Pago no devolvió el enlace de pago (checkout_url)." }, 500);
    }

    return json({ ok: true, order: orderId, url: order.checkout_url });
  } catch (e) {
    return json({ error: e.message }, 500);
  }
}

async function handlePaymentWebhook(request, env, url) {
  try {
    if (!getMpToken(env)) {
      // No romper: responder 200 para que MP no reintente en bucle
      return json({ ok: true, skipped: true, reason: "MP_ACCESS_TOKEN no configurada." });
    }

    // MP notifica el evento "Order (Mercado Pago)" a la URL configurada
    // en el dashboard de la app (Webhooks → Configure notifications).
    // El payload trae solo un id — nunca se confía en su contenido:
    // siempre se re-consulta el recurso completo contra MP.
    let resourceId = null;
    if (request.method === "POST") {
      const body = await request.json().catch(() => ({}));
      resourceId =
        (body.data && body.data.id) ||
        (body.resource && String(body.resource).split("/").filter(Boolean).pop()) ||
        null;
    }
    if (!resourceId) {
      const q = new URL(request.url).searchParams;
      resourceId = q.get("data.id") || q.get("id") || q.get("collection_id");
    }
    if (!resourceId) return json({ ok: true, ignored: true });

    // 1) Intentar como ORDEN (flujo nuevo, Checkout Pro vía Orders API)
    const orderRes = await fetch(
      `https://api.mercadopago.com/v1/orders/${encodeURIComponent(resourceId)}`,
      { headers: { "Authorization": "Bearer " + getMpToken(env) } }
    );

    let orderId, mpResourceId, paidAmount;
    if (orderRes.ok) {
      const ord = await orderRes.json();
      orderId = ord.external_reference;
      mpResourceId = String(ord.id);
      paidAmount = Number(ord.total_paid_amount || 0);
      const isPaid = ord.status === "processed" && paidAmount + 0.01 >= Number(ord.total_amount || 0);
      if (!isPaid) return json({ ok: true, status: ord.status || "unknown" });
    } else {
      // 2) Fallback: formato clásico de notificación por pago
      const payRes = await fetch(
        `https://api.mercadopago.com/v1/payments/${encodeURIComponent(resourceId)}`,
        { headers: { "Authorization": "Bearer " + getMpToken(env) } }
      );
      if (!payRes.ok) return json({ ok: true, ignored: true });
      const pay = await payRes.json();
      if (pay.status !== "approved") return json({ ok: true, status: pay.status || "unknown" });
      orderId = pay.external_reference;
      mpResourceId = String(pay.id);
      paidAmount = Number(pay.transaction_amount || 0);
    }

    if (!orderId) return json({ ok: true, ignored: true });

    // Acreditar (RPC idempotente; verifica paquete activo y monto)
    const rpcRes = await fetch(`${env.SUPABASE_URL}/rest/v1/rpc/process_payment_webhook`, {
      method: "POST",
      headers: {
        "apikey": env.SUPABASE_SERVICE_ROLE_KEY,
        "Authorization": "Bearer " + env.SUPABASE_SERVICE_ROLE_KEY,
        "Content-Type": "application/json",
      },
      body: JSON.stringify({
        p_order: orderId,
        p_mp_payment_id: mpResourceId,
        p_amount: paidAmount,
      }),
    });
    const result = rpcRes.ok ? await rpcRes.json() : { ok: false, reason: "rpc_error" };
    return json({ ok: true, credited: result });
  } catch (e) {
    return json({ ok: true, error: e.message }); // 200 siempre: MP reintenta si no
  }
}

// ═══════════════════════════════════════════════════════════════════

export default {
  async fetch(request, env) {
    const url = new URL(request.url);
    const path = url.pathname;
    const method = request.method;
    const params = url.searchParams;

    // Si no es una ruta de API, servir los archivos estáticos de la página
    if (!path.startsWith("/api/")) {
      return env.ASSETS.fetch(request);
    }

    if (method === "OPTIONS") return new Response(null, { headers: corsHeaders });

    // ═══ Confirmación de correo (Resend) ═══
    // Rutas PÚBLICAS por diseño: no exponen datos.
    //  - /api/email/send solo envía un correo a cuentas existentes
    //    pendientes de confirmar (con rate limit).
    //  - /api/email/verify solo valida un token firmado y marca
    //    email_confirmed=true. Nada más.
    if (path === "/api/email/send" && method === "POST") {
      return handleSendConfirm(request, env, url);
    }
    if (path === "/api/email/verify" && method === "POST") {
      return handleVerifyConfirm(request, env);
    }

    // ═══ Pagos (Mercado Pago) ═══
    //  - /api/payments/create: requiere el token de sesión de Supabase
    //    del agente (se verifica contra Supabase). Crea la orden y el
    //    checkout de Mercado Pago.
    //  - /api/payments/webhook: la llama MP tras el pago. NO se confía
    //    en el payload: el pago se re-verifica contra los servidores de
    //    MP y los créditos los acredita el RPC process_payment_webhook.
    if (path === "/api/debug/mp-cancel" && method === "GET") {
      const tok = getMpToken(env) || "";
      const r = await fetch("https://api.mercadopago.com/v1/orders/ORD01M2NQWWE4S3BWBJS1H74KFJM8/cancel", {
        method: "POST",
        headers: { "Authorization": "Bearer " + tok, "Content-Type": "application/json", "X-Idempotency-Key": "cleanup-debug-1" },
      });
      const data = await r.json().catch(() => ({}));
      return json({ http: r.status, status: data.status || data.message || null });
    }

    if (path === "/api/payments/create" && method === "POST") {
      return handlePaymentCreate(request, env, url);
    }
    if (path === "/api/payments/webhook" && (method === "POST" || method === "GET")) {
      return handlePaymentWebhook(request, env, url);
    }

    // Seguridad: este proxy usa SERVICE_ROLE (bypass RLS), así que debe
    // quedar CERRADO por defecto. Requiere API_KEY configurada en
    // Cloudflare (wrangler secret put API_KEY) y el header x-api-key.
    if (!env.API_KEY) {
      return new Response(JSON.stringify({ error: "API proxy disabled: configure API_KEY en Cloudflare." }), {
        status: 403,
        headers: corsHeaders
      });
    }
    if (request.headers.get("x-api-key") !== env.API_KEY) {
      return new Response(JSON.stringify({ error: "Unauthorized" }), {
        status: 401,
        headers: corsHeaders
      });
    }

    try {
      const segments = path.split("/").filter(Boolean);
      const collection = segments.pop();

      if (!collection) {
        return new Response(JSON.stringify({ error: "No collection specified" }), {
          status: 400,
          headers: { ...corsHeaders, "Content-Type": "application/json" }
        });
      }

      // Manejo especial para subida de archivos
      if (collection === "upload" && method === "POST") {
        const fileName = params.get("name") || `file_${Date.now()}.pdf`;
        const blob = await request.arrayBuffer();
        // Subir a bucket 'dc3' (ajustar según sea necesario)
        const data = await sbFetch(`dc3/${fileName}`, "POST", blob, "", env, true);
        return new Response(JSON.stringify({ uploaded: data }), {
          headers: { ...corsHeaders, "Content-Type": "application/json" }
        });
      }


      let supabaseQuery = "?select=*";
      params.forEach((val, key) => {
        supabaseQuery += `&${key}=eq.${val}`;
      });

      if (method === "GET") {
        const data = await sbFetch(collection, "GET", null, supabaseQuery, env);
        return new Response(JSON.stringify({ documents: data }), {
          headers: { ...corsHeaders, "Content-Type": "application/json" }
        });
      }

      if (method === "POST" || method === "PUT") {
        const body = await request.json();
        const doc = body.documents || body.document || body;
        const data = await sbFetch(collection, method, doc, method === "PUT" ? `?id=eq.${doc.id}` : "", env);
        return new Response(JSON.stringify({ result: data }), {
          headers: { ...corsHeaders, "Content-Type": "application/json" }
        });
      }

      if (method === "DELETE") {
        const body = await request.json();
        const data = await sbFetch(collection, "DELETE", null, `?id=eq.${body.id}`, env);
        return new Response(JSON.stringify({ deleted: data }), {
          headers: { ...corsHeaders, "Content-Type": "application/json" }
        });
      }

      return new Response("Not Found", { status: 404 });
    } catch (e) {
      return new Response(JSON.stringify({ error: e.message }), {
        status: 500,
        headers: { ...corsHeaders, "Content-Type": "application/json" }
      });
    }
  }
};
