/**
 * El frontend es 100 % de componentes cliente ("use client"): no usa SSR, API routes ni
 * middleware. Por eso se compila como sitio estatico (carpeta out/), que Spring Boot sirve
 * junto con la API en un unico despliegue. "npm run dev" sigue funcionando igual en :3000.
 */
const nextConfig = {
  output: "export",
  images: { unoptimized: true }
};

export default nextConfig;
