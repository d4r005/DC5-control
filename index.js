/**
 * DC5-control Cloudflare Worker
 * Backend API con Supabase - Corregido para filtrado de datos
 */

const SUPABASE_URL = "https://osgfwgedjdltrmvwycjd.supabase.co";
const SUPABASE_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6Im9zZ2Z3Z2VkamRsdHJtdnd5Y2pkIiwicm9sZSI6ImFub24iLCJpYXQiOjE3ODQyNDAxMzcsImV4cCI6MjA5OTgxNjEzN30.jeV98eAfhQzkXiGj88DUOLqLPLFr_IKPrcnTaefEgj0";

const corsHeaders = {
  "Access-Control-Allow-Origin": "*",
  "Access-Control-Allow-Methods": "GET, POST, PUT, DELETE, OPTIONS",
  "Access-Control-Allow-Headers": "Content-Type, Authorization, apikey",
};

async function sbFetch(table, method, body, queryParams = "") {
  const headers = {
    "apikey": SUPABASE_KEY,
    "Authorization": `Bearer ${SUPABASE_KEY}`,
    "Content-Type": "application/json",
  };

  // Supabase usa ?select=* y filtros como &campo=eq.valor
  let url = `${SUPABASE_URL}/rest/v1/${table}${queryParams}`;

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
      // Si llega ?creatorEmail=valor -> &creatorEmail=eq.valor
      params.forEach((val, key) => {
        supabaseQuery += `&${key}=eq.${val}`;
      });

      if (method === "GET") {
        const data = await sbFetch(collection, "GET", null, supabaseQuery);
        return new Response(JSON.stringify({ documents: data }), {
          headers: { ...corsHeaders, "Content-Type": "application/json" }
        });
      }

      if (method === "POST") {
        const body = await request.json();
        const doc = body.documents || body.document || body;
        const data = await sbFetch(collection, "POST", doc);
        return new Response(JSON.stringify({ inserted: data }), {
          headers: { ...corsHeaders, "Content-Type": "application/json" }
        });
      }

      if (method === "DELETE") {
        const body = await request.json();
        const data = await sbFetch(collection, "DELETE", null, `?id=eq.${body.id}`);
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
