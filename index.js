/**
 * DC5-control Cloudflare Worker
 * Backend API con Supabase
 * 
 * Aunque el index.html usa Supabase JS client directamente,
 * este Worker queda como API de respaldo para la app Android.
 */

const SUPABASE_URL = "https://osgfwgedjdltrmvwycjd.supabase.co";
const SUPABASE_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6Im9zZ2Z3Z2VkamRsdHJtdnd5Y2pkIiwicm9sZSI6ImFub24iLCJpYXQiOjE3ODQyNDAxMzcsImV4cCI6MjA5OTgxNjEzN30.jeV98eAfhQzkXiGj88DUOLqLPLFr_IKPrcnTaefEgj0";

const corsHeaders = {
  "Access-Control-Allow-Origin": "*",
  "Access-Control-Allow-Methods": "GET, POST, PUT, DELETE, OPTIONS",
  "Access-Control-Allow-Headers": "Content-Type, Authorization, apikey",
};

async function sbFetch(table, method, body) {
  const headers = {
    "apikey": SUPABASE_KEY,
    "Authorization": `Bearer ${SUPABASE_KEY}`,
    "Content-Type": "application/json",
  };
  let url = `${SUPABASE_URL}/rest/v1/${table}`;

  const opts = { method, headers };

  if (method === "GET") {
    opts.headers["Accept"] = "application/json";
  } else if (method === "POST") {
    opts.headers["Prefer"] = "return=representation";
    opts.body = JSON.stringify(body);
  } else if (method === "DELETE") {
    url += `?id=eq.${body.id}`;
  }

  const res = await fetch(url, opts);
  return await res.json();
}

export default {
  async fetch(request, env) {
    const url = new URL(request.url);
    const path = url.pathname;
    const method = request.method;

    if (method === "OPTIONS") {
      return new Response(null, { headers: corsHeaders });
    }

    try {
      // ── Workers ─────────────────────────────────────────────
      if (path === "/api/workers" && method === "GET") {
        const data = await sbFetch("workers?select=*", "GET");
        return new Response(JSON.stringify({ documents: data }), {
          headers: { ...corsHeaders, "Content-Type": "application/json" }
        });
      }
      if (path === "/api/workers" && method === "POST") {
        const body = await request.json();
        const docs = body.documents || [body.document || body];
        const data = await sbFetch("workers", "POST", docs);
        return new Response(JSON.stringify({ inserted: data }), {
          headers: { ...corsHeaders, "Content-Type": "application/json" }
        });
      }

      // ── Companies ───────────────────────────────────────────
      if (path === "/api/companies" && method === "GET") {
        const data = await sbFetch("companies?select=*", "GET");
        return new Response(JSON.stringify({ documents: data }), {
          headers: { ...corsHeaders, "Content-Type": "application/json" }
        });
      }
      if (path === "/api/companies" && method === "POST") {
        const body = await request.json();
        const doc = body.document || body;
        const data = await sbFetch("companies", "POST", doc);
        return new Response(JSON.stringify({ inserted: data }), {
          headers: { ...corsHeaders, "Content-Type": "application/json" }
        });
      }

      // ── Courses ──────────────────────────────────────────────
      if (path === "/api/courses" && method === "GET") {
        const data = await sbFetch("courses?select=*", "GET");
        return new Response(JSON.stringify({ documents: data }), {
          headers: { ...corsHeaders, "Content-Type": "application/json" }
        });
      }
      if (path === "/api/courses" && method === "POST") {
        const body = await request.json();
        const doc = body.document || body;
        const data = await sbFetch("courses", "POST", doc);
        return new Response(JSON.stringify({ inserted: data }), {
          headers: { ...corsHeaders, "Content-Type": "application/json" }
        });
      }

      // ── Agents ───────────────────────────────────────────────
      if (path === "/api/agents" && method === "GET") {
        const data = await sbFetch("agents?select=*", "GET");
        return new Response(JSON.stringify({ documents: data }), {
          headers: { ...corsHeaders, "Content-Type": "application/json" }
        });
      }
      if (path === "/api/agents" && method === "POST") {
        const body = await request.json();
        const doc = body.document || body;
        const data = await sbFetch("agents", "POST", doc);
        return new Response(JSON.stringify({ inserted: data }), {
          headers: { ...corsHeaders, "Content-Type": "application/json" }
        });
      }

      // ── DC-3 Records ─────────────────────────────────────────
      if (path === "/api/dc3" && method === "GET") {
        const data = await sbFetch("dc3_records?select=*", "GET");
        return new Response(JSON.stringify({ documents: data }), {
          headers: { ...corsHeaders, "Content-Type": "application/json" }
        });
      }
      if (path === "/api/dc3" && method === "POST") {
        const body = await request.json();
        const doc = body.document || body;
        const data = await sbFetch("dc3_records", "POST", doc);
        return new Response(JSON.stringify({ inserted: data }), {
          headers: { ...corsHeaders, "Content-Type": "application/json" }
        });
      }

      // ── Health ───────────────────────────────────────────────
      if (path === "/" || path === "/api") {
        return new Response(JSON.stringify({
          status: "OK",
          backend: "Supabase",
          url: SUPABASE_URL,
          endpoints: ["/api/workers", "/api/companies", "/api/courses", "/api/agents", "/api/dc3"]
        }), {
          headers: { ...corsHeaders, "Content-Type": "application/json" }
        });
      }

      return new Response(JSON.stringify({ error: "Not found", path }), {
        status: 404,
        headers: { ...corsHeaders, "Content-Type": "application/json" }
      });

    } catch (e) {
      return new Response(JSON.stringify({ error: e.message }), {
        status: 500,
        headers: { ...corsHeaders, "Content-Type": "application/json" }
      });
    }
  }
};
