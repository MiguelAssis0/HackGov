import { useInsertionEffect, useLayoutEffect } from "react";

const styleEffect = useInsertionEffect || useLayoutEffect;

function ensureStylesheet(href, group) {
  let link = document.querySelector(`link[data-react-page-style="${href}"]`);

  if (!link) {
    link = document.createElement("link");
    link.rel = "stylesheet";
    link.href = href;
    link.dataset.reactPageStyle = href;
    link.dataset.reactStyleGroup = group;
    document.head.appendChild(link);
  }

  link.disabled = false;
  return link;
}

export function usePageStyles(paths, group = "page") {
  styleEffect(() => {
    const active = new Set(paths);

    paths.forEach((href) => ensureStylesheet(href, group));

    document
      .querySelectorAll(`link[data-react-style-group="${group}"]`)
      .forEach((link) => {
        link.disabled = !active.has(link.dataset.reactPageStyle);
      });
  }, [paths.join("|"), group]);
}
