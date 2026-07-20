/**
 * DC5-control Cloudflare Worker
 * Backend API con Supabase
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

export default {
  async fetch(request, env) {
    const url = new URL(request.url);
    const path = url.pathname;
    const method = request.method;
    const params = url.searchParams;

    if (method === "OPTIONS") return new Response(null, { headers: corsHeaders });

    // Seguridad básica (opcional, configurar API_KEY en Cloudflare)
    if (env.API_KEY && request.headers.get("x-api-key") !== env.API_KEY) {
      return new Response(JSON.stringify({ error: "Unauthorized" }), {
        status: 401,
        headers: corsHeaders
      });
    }

    try {
      const segments = path.split("/").filter(Boolean);
      const collection = segments.pop();

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
