function dec2hex(dec: any) {
  return dec.toString(16).padStart(2, "0");
}
export function generateRandomText(len: number) {
  const arr = new Uint8Array(len);
  window.crypto.getRandomValues(arr);

  return Array.from(arr, dec2hex).join("");
}

export default generateRandomText;
