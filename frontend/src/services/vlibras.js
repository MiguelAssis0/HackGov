// ponytail: plugin v7 monta #vlibras-access-wrapper no body (o [vw] é legado v5 e sozinho não remove nada)
const SCRIPT_ID = "vlibras-plugin-script";
const WIDGET_URL = "https://vlibras.gov.br/app";

export function enableVLibras() {
  if (document.getElementById("vlibras-access-wrapper")) return;
  // script novo = closure nova (o plugin ignora o 2º new Widget() da mesma carga)
  document.getElementById(SCRIPT_ID)?.remove();
  document.querySelectorAll("#vlibras-access-wrapper, #vlibras-app-root, [vw]").forEach((el) => el.remove());
  const el = document.createElement("script");
  el.id = SCRIPT_ID;
  el.src = `${WIDGET_URL}/vlibras-plugin.js`;
  el.onload = () => {
    try {
      window.VLibras && new window.VLibras.Widget(WIDGET_URL);
    } catch {
      /* widget opcional */
    }
  };
  document.body.appendChild(el);
}

export function disableVLibras() {
  document.getElementById(SCRIPT_ID)?.remove();
  document.querySelectorAll('script[src*="vlibras-plugin-app"]').forEach((el) => el.remove());
  document.querySelectorAll("#vlibras-access-wrapper, #vlibras-app-root, [vw]").forEach((el) => el.remove());
}
