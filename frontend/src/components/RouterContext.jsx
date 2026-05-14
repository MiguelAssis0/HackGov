import { createContext, useContext } from "react";

export const RouterContext = createContext({
  path: "/",
  navigate: () => {},
});

export function useRouter() {
  return useContext(RouterContext);
}

export function Link({ to, className, children, onClick, ...props }) {
  const { navigate } = useRouter();

  function handleClick(event) {
    if (onClick) onClick(event);
    if (
      event.defaultPrevented ||
      event.metaKey ||
      event.ctrlKey ||
      event.shiftKey ||
      event.altKey ||
      props.target === "_blank"
    ) {
      return;
    }

    event.preventDefault();
    navigate(to);
  }

  return (
    <a href={to} className={className} onClick={handleClick} {...props}>
      {children}
    </a>
  );
}
