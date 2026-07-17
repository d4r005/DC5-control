/**
 * DC5-control Cloudflare Worker
 * Backend API con Supabase
 */

const corsHeaders = {
  "Access-Control-Allow-Origin": "*",
  "Access-Control-Allow-Methods": "GET, POST, PUT, DELETE, OPTIONS",
  "Access-Control-Allow-Headers": "Content-Type, Authorization, apikey",
};

async function sbFetch(table, method, body, queryParams, env) {
  const supabaseUrl = env.SUPABASE_URL;
  const supabaseKey = env.SUPABASE_SERVICE_ROLE_KEY;

  const headers = {
    "apikey": supabaseKey,
    "Authorization": `Bearer ${supabaseKey}`,
    "Content-Type": "application/json",
  };

  let url = `${supabaseUrl}/rest/v1/${table}${queryParams}`;

  const opts = { method, headers };
  if (method === "POST") {
    opts.headers["Prefer"] = "return=representation";
    opts.body = JSON.stringify(body);
  }

  const res = await fetch(url, opts);
  return await res.json();
}

export default {
  async fetch(request, env) {
    const url = new URL(request.url);
    const path = url.pathname;
    const method = request.method;
    const params = url.searchParams;

    if (method === "OPTIONS") return new Response(null, { headers: corsHeaders });

    try {
      const collection = path.split("/").pop();
      let supabaseQuery = "?select=*";

      // Mapear parámetros de búsqueda a filtros de Supabase
      params.forEach((val, key) => {
        supabaseQuery += `&${key}=eq.${val}`;
      });

      if (method === "GET") {
        const data = await sbFetch(collection, "GET", null, supabaseQuery, env);
        return new Response(JSON.stringify({ documents: data }), {
          headers: { ...corsHeaders, "Content-Type": "application/json" }
        });
      }

      if (method === "POST") {
        const body = await request.json();
        const doc = body.documents || body.document || body;
        const data = await sbFetch(collection, "POST", doc, "", env);
        return new Response(JSON.stringify({ inserted: data }), {
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
