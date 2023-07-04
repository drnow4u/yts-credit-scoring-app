export function readCookie(cookie: string | undefined) {
  if (!cookie) {
    throw new Error("Cookie not present");
  }

  return JSON.parse(cookie);
}

export function readSessionStorage<T = any>(key: string, fallback?: T) {
  const data = sessionStorage.getItem(key);

  if (!data) {
    if (fallback) {
      return fallback;
    }
    throw new Error("Data not present");
  }

  return JSON.parse(data) as T;
}
