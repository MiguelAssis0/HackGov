import { useLayoutEffect, useState } from "react";

const loadedStyles = new Set();
const loadingStyles = new Map();

function styleKey(href) {
  return new URL(href, document.baseURI).href;
}

function findStylesheet(href) {
  const key = styleKey(href);

  return Array.from(document.querySelectorAll('link[rel="stylesheet"]')).find(
    (link) =>
      link.href === key ||
      link.dataset.reactPageStyle === key ||
      link.getAttribute("href") === href,
  );
}

function markStylesheetLoaded(link, key) {
  loadedStyles.add(key);
  link.dataset.reactStyleLoaded = "true";
}

function isStylesheetLoaded(link, key) {
  if (!link) return false;
  if (loadedStyles.has(key) || link.dataset.reactStyleLoaded === "true") return true;

  return false;
}

function areStylesLoaded(paths) {
  return paths.every((href) => {
    const key = styleKey(href);
    return isStylesheetLoaded(findStylesheet(href), key);
  });
}

function ensureStylesheet(href, group) {
  const key = styleKey(href);
  let link = findStylesheet(href);
  const created = !link;

  if (!link) {
    link = document.createElement("link");
    link.rel = "stylesheet";
    link.href = href;
  }

  link.dataset.reactPageStyle = key;
  link.dataset.reactStyleGroup = group;
  link.disabled = false;

  if (isStylesheetLoaded(link, key)) {
    return Promise.resolve(link);
  }

  if (loadingStyles.has(key)) {
    return loadingStyles.get(key);
  }

  const promise = new Promise((resolve) => {
    let timeoutId;

    const finish = () => {
      window.clearTimeout(timeoutId);
      link.removeEventListener("load", finish);
      link.removeEventListener("error", finish);
      markStylesheetLoaded(link, key);
      resolve(link);
    };

    link.addEventListener("load", finish);
    link.addEventListener("error", finish);
    timeoutId = window.setTimeout(finish, 8000);
  });

  loadingStyles.set(key, promise);
  promise.finally(() => loadingStyles.delete(key));

  if (created) {
    document.head.appendChild(link);
  }

  return promise;
}

export function usePageStyles(paths, group = "page") {
  const stylePaths = Array.from(new Set(paths.filter(Boolean)));
  const signature = stylePaths.join("|");
  const [ready, setReady] = useState(() => stylePaths.length === 0 || areStylesLoaded(stylePaths));

  useLayoutEffect(() => {
    let cancelled = false;

    if (stylePaths.length === 0) {
      setReady(true);
      return () => {
        cancelled = true;
      };
    }

    setReady(false);

    Promise.all(stylePaths.map((href) => ensureStylesheet(href, group))).then(() => {
      if (cancelled) return;
      window.requestAnimationFrame(() => {
        if (!cancelled) setReady(true);
      });
    });

    return () => {
      cancelled = true;
    };
  }, [signature, group]);

  return ready;
}
