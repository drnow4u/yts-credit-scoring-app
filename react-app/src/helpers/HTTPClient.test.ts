// Cannot be imported because the module would get initialized before we can set our localStorage
const USER_TOKEN_STORAGE_NAME = "user_token";
const TOKEN_HEADER_NAME = "Authorization";
const jwtToken = "Bearer TEST_JWT_TOKEN";

beforeAll(() => {
  localStorage.setItem(USER_TOKEN_STORAGE_NAME, jwtToken);
});

test("PublicHTTPClient sets Authorization header from localStorage", async () => {
  const { PublicHTTPClient } = await import("./HTTPClient");

  // https://github.com/axios/axios/issues/4193
  // @ts-ignore
  expect(PublicHTTPClient.defaults.headers[TOKEN_HEADER_NAME]).toBe(jwtToken);
});

export {};
