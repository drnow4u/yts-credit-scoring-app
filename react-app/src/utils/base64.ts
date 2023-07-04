function base64ToUint8Array(base64: string) {
  return new Uint8Array(
    atob(base64)
      .split("")
      .map((c) => c.charCodeAt(0))
  );
}

function stringToBase64(string: string) {
  const encodedString = new TextEncoder().encode(string);
  const binary = [];
  const bytes = new Uint8Array(encodedString);
  for (let i = 0, il = bytes.byteLength; i < il; i++) {
    binary.push(String.fromCharCode(bytes[i]));
  }
  return btoa(binary.join(""));
}

function base64ToString(base64: string) {
  const encodedString = Uint8Array.from(atob(base64), (c) => c.charCodeAt(0));
  return new TextDecoder().decode(encodedString);
}

export { base64ToString, base64ToUint8Array, stringToBase64 };
