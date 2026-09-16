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
  const b64 = str.replace(/-/g, "+").replace(/_/g, "/");
  return decodeURIComponent(escape(atob(b64)));
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

    const from = env.RESEND_FROM_EMAIL || "ACE Control <notificaciones@ehs-solutions.online>";
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
