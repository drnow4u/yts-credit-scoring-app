import { base64ToString, stringToBase64 } from "./base64";

describe("base64 encoding/decoding", () => {
  test("encode simple string", () => {
    // given
    const text =
      "Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat. Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur. Excepteur sint occaecat cupidatat non proident, sunt in culpa qui officia deserunt mollit anim id est laborum.";
    const expected =
      "TG9yZW0gaXBzdW0gZG9sb3Igc2l0IGFtZXQsIGNvbnNlY3RldHVyIGFkaXBpc2NpbmcgZWxpdCwgc2VkIGRvIGVpdXNtb2QgdGVtcG9yIGluY2lkaWR1bnQgdXQgbGFib3JlIGV0IGRvbG9yZSBtYWduYSBhbGlxdWEuIFV0IGVuaW0gYWQgbWluaW0gdmVuaWFtLCBxdWlzIG5vc3RydWQgZXhlcmNpdGF0aW9uIHVsbGFtY28gbGFib3JpcyBuaXNpIHV0IGFsaXF1aXAgZXggZWEgY29tbW9kbyBjb25zZXF1YXQuIER1aXMgYXV0ZSBpcnVyZSBkb2xvciBpbiByZXByZWhlbmRlcml0IGluIHZvbHVwdGF0ZSB2ZWxpdCBlc3NlIGNpbGx1bSBkb2xvcmUgZXUgZnVnaWF0IG51bGxhIHBhcmlhdHVyLiBFeGNlcHRldXIgc2ludCBvY2NhZWNhdCBjdXBpZGF0YXQgbm9uIHByb2lkZW50LCBzdW50IGluIGN1bHBhIHF1aSBvZmZpY2lhIGRlc2VydW50IG1vbGxpdCBhbmltIGlkIGVzdCBsYWJvcnVtLg==";
    // when
    const base64 = stringToBase64(text);
    // then
    expect(base64).toEqual(expected);
  });

  test("decode simple string", () => {
    // given
    const expected =
      "Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat. Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur. Excepteur sint occaecat cupidatat non proident, sunt in culpa qui officia deserunt mollit anim id est laborum.";
    const base64 =
      "TG9yZW0gaXBzdW0gZG9sb3Igc2l0IGFtZXQsIGNvbnNlY3RldHVyIGFkaXBpc2NpbmcgZWxpdCwgc2VkIGRvIGVpdXNtb2QgdGVtcG9yIGluY2lkaWR1bnQgdXQgbGFib3JlIGV0IGRvbG9yZSBtYWduYSBhbGlxdWEuIFV0IGVuaW0gYWQgbWluaW0gdmVuaWFtLCBxdWlzIG5vc3RydWQgZXhlcmNpdGF0aW9uIHVsbGFtY28gbGFib3JpcyBuaXNpIHV0IGFsaXF1aXAgZXggZWEgY29tbW9kbyBjb25zZXF1YXQuIER1aXMgYXV0ZSBpcnVyZSBkb2xvciBpbiByZXByZWhlbmRlcml0IGluIHZvbHVwdGF0ZSB2ZWxpdCBlc3NlIGNpbGx1bSBkb2xvcmUgZXUgZnVnaWF0IG51bGxhIHBhcmlhdHVyLiBFeGNlcHRldXIgc2ludCBvY2NhZWNhdCBjdXBpZGF0YXQgbm9uIHByb2lkZW50LCBzdW50IGluIGN1bHBhIHF1aSBvZmZpY2lhIGRlc2VydW50IG1vbGxpdCBhbmltIGlkIGVzdCBsYWJvcnVtLg==";
    // when
    const text = base64ToString(base64);
    // then
    expect(text).toEqual(expected);
  });

  test("encode some runes", () => {
    // given
    const text = "ᚠᚢᚦᚨᚱᛗᛞ";
    const expected = "4Zqg4Zqi4Zqm4Zqo4Zqx4ZuX4Zue";
    // when
    const base64 = stringToBase64(text);
    // then
    expect(base64).toEqual(expected);
  });

  test("decode some runes", () => {
    // given
    const expected = "ᚠᚢᚦᚨᚱᛗᛞ";
    const base64 = "4Zqg4Zqi4Zqm4Zqo4Zqx4ZuX4Zue";
    // when
    const text = base64ToString(base64);
    // then
    expect(text).toEqual(expected);
  });

  test("in japanese!", () => {
    // given
    const text = "かくて勇者は旅立たん";
    const expected = "44GL44GP44Gm5YuH6ICF44Gv5peF56uL44Gf44KT";
    // when
    const base64 = stringToBase64(text);
    // then
    expect(base64).toEqual(expected);
  });

  test("from japanese", () => {
    // given
    const expected = "かくて勇者は旅立たん";
    const base64 = "44GL44GP44Gm5YuH6ICF44Gv5peF56uL44Gf44KT";
    // when
    const text = base64ToString(base64);
    // then
    expect(text).toEqual(expected);
  });

  test("encode json", () => {
    // given
    const object = {
      text1: "Lorem Ipsum",
      ᚠᚢᚦᚨᚱᛗᛞ: "かくて勇者は旅立たん",
    };
    const text = JSON.stringify(object);
    const expected =
      "eyJ0ZXh0MSI6IkxvcmVtIElwc3VtIiwi4Zqg4Zqi4Zqm4Zqo4Zqx4ZuX4ZueIjoi44GL44GP44Gm5YuH6ICF44Gv5peF56uL44Gf44KTIn0=";
    // when
    const base64 = stringToBase64(text);
    // then
    expect(base64).toEqual(expected);
  });

  test("decode json", () => {
    // given
    const expectedObject = {
      text1: "Lorem Ipsum",
      ᚠᚢᚦᚨᚱᛗᛞ: "かくて勇者は旅立たん",
    };
    const base64 =
      "eyJ0ZXh0MSI6IkxvcmVtIElwc3VtIiwi4Zqg4Zqi4Zqm4Zqo4Zqx4ZuX4ZueIjoi44GL44GP44Gm5YuH6ICF44Gv5peF56uL44Gf44KTIn0=";
    // when
    const text = base64ToString(base64);
    const object = JSON.parse(text) as typeof expectedObject;
    // then
    expect(object).toEqual(expectedObject);
  });
});
