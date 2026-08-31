import { defineConfig } from "vite";
import react from "@vitejs/plugin-react";

export default defineConfig({
  plugins: [react()],
  server: {
    port: 5173,
    // ponytail: hackgov-backend só resolve dentro do docker-network; no host (npm run dev) cai em ENOTFOUND
    // usa localhost por padrão e permite override via VITE_PROXY_TARGET para rodar dentro do container
    proxy: {
      "/api": {
        target: process.env.VITE_PROXY_TARGET || "http://localhost:8080",
        changeOrigin: true,
      },
      "/ws": {
        target: (process.env.VITE_PROXY_TARGET || "http://localhost:8080").replace(/^http/, "ws"),
        changeOrigin: true,
        ws: true,
      },
    },
  },
});
