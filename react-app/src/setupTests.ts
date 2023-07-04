import faker from "@faker-js/faker";
import { TextDecoder, TextEncoder } from "util";

import i18n from "./i18n/config";
import { server } from "./test/server";

// jest-dom adds custom jest matchers for asserting on DOM nodes.
// allows you to do things like:
// expect(element).toHaveTextContent(/react/i)
// learn more: https://github.com/testing-library/jest-dom
import "@testing-library/jest-dom";
import "mock-local-storage";

// Add TextEncoder and TextDecoder as they're missing in jsdom env
global.TextEncoder = TextEncoder;
// @ts-expect-error
global.TextDecoder = TextDecoder;

faker.setLocale("en");
i18n.changeLanguage("EN");

jest.setTimeout(30000);

beforeAll(() => server.listen());

// if you need to add a handler after calling setupServer for some specific test
// this will remove that handler for the rest of them
// (which is important for test isolation):
afterEach(() => server.resetHandlers());
afterAll(() => server.close());
