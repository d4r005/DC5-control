export default {
  async fetch(request, env) {
    const url = new URL(request.url);
    const path = url.pathname;
    const method = request.method;

    // Configuración de MongoDB Atlas Data API
    const ATLAS_API_URL = "https://data.mongodb-api.com/app/data-jshze/endpoint/data/v1/action";
    const ATLAS_API_KEY = "al-b14XHfG6V17yJMOy1lmUWm9vCbuLZG1-Ot7Mp0YAMhH";
    const CLUSTER = "acecontrol";
    const DATABASE = "DC3_Database";

    async function queryAtlas(action, collection, additionalParams = {}) {
      const response = await fetch(`${ATLAS_API_URL}/${action}`, {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
          "api-key": ATLAS_API_KEY
        },
        body: JSON.stringify({
          dataSource: CLUSTER,
          database: DATABASE,
          collection: collection,
          ...additionalParams
        })
      });
      return await response.json();
    }

    // CORS Headers
    const corsHeaders = {
      "Access-Control-Allow-Origin": "*",
      "Access-Control-Allow-Methods": "GET, POST, OPTIONS",
      "Access-Control-Allow-Headers": "Content-Type, api-key",
    };

    if (method === "OPTIONS") {
      return new Response(null, { headers: corsHeaders });
    }

    try {
      if (path === "/workers" && method === "POST") {
        const data = await request.json();
        const result = await queryAtlas("insertMany", "workers", { documents: data.documents });
        return new Response(JSON.stringify(result), { headers: { ...corsHeaders, "Content-Type": "application/json" } });
      }

      if (path === "/courses" && method === "GET") {
        const result = await queryAtlas("find", "courses", { filter: {} });
        return new Response(JSON.stringify(result), { headers: { ...corsHeaders, "Content-Type": "application/json" } });
      }

      if (path === "/courses" && method === "POST") {
        const data = await request.json();
        const result = await queryAtlas("insertOne", "courses", { document: data.document });
        return new Response(JSON.stringify(result), { headers: { ...corsHeaders, "Content-Type": "application/json" } });
      }

      if (path === "/agents" && method === "GET") {
        const result = await queryAtlas("find", "agents", { filter: {} });
        return new Response(JSON.stringify(result), { headers: { ...corsHeaders, "Content-Type": "application/json" } });
      }

      if (path === "/agents" && method === "POST") {
        const data = await request.json();
        const result = await queryAtlas("insertOne", "agents", { document: data.document });
        return new Response(JSON.stringify(result), { headers: { ...corsHeaders, "Content-Type": "application/json" } });
      }

      return new Response("DC5-control API Activa", { status: 200, headers: corsHeaders });
    } catch (e) {
      return new Response(JSON.stringify({ error: e.message }), { status: 500, headers: corsHeaders });
    }
  }
};
