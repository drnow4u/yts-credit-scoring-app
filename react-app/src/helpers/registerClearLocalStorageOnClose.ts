import { HOMEPAGE } from "helpers/constants";

const terminationEvent = "onpagehide" in window.self ? "pagehide" : "unload";

const excludedPaths: string[] = [
  `${HOMEPAGE}select-bank`,
  `${HOMEPAGE}site-connect-callback`,
  `/site-connect-callback/`,
];

function clearLocalStorage() {
  if (!excludedPaths.includes(window.location.pathname)) {
    localStorage.clear();
  }
}

export function registerClearLocalStorageOnClose() {
  window.addEventListener(terminationEvent, clearLocalStorage, {
    capture: true,
  });
}
