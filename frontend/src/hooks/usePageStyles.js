import { useEffect } from "react";

export function usePageStyles(paths) {
  useEffect(() => {
    const links = paths.map((href) => {
      const link = document.createElement("link");
      link.rel = "stylesheet";
      link.href = href;
      link.dataset.reactPageStyle = "true";
      document.head.appendChild(link);
      return link;
    });

    return () => {
      links.forEach((link) => link.remove());
    };
  }, [paths.join("|")]);
}
