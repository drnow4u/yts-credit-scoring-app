import userEvent from "@testing-library/user-event";
import translationEN from "i18n/en/translation.json";
import { Route, Routes } from "react-router-dom";

import {
  defaultCustomRenderOptions,
  render,
  screen,
  waitFor,
} from "test/utils/test-utils";

import Consent from "./Consent";

// todo - flaky test, fails on pipeline
test.skip("renders the client name", async () => {
  const userHash = "examplehash123";
  const initialUrl = `/consent/${userHash}`;
  const { queryResponses } = render(
    <Routes>
      <Route path="/consent/:userHash" element={<Consent />} />
    </Routes>,
    {
      ...defaultCustomRenderOptions,
      initialUrl,
    }
  );
  const headingElement = await screen.findByTestId("subtitle-client-logo");

  expect(headingElement).toBeInTheDocument();
  await waitFor(() => expect(headingElement).toBeInTheDocument());

  const { response } = queryResponses["client"];
  expect(response).toHaveProperty("name");
  expect(response).toHaveProperty("language");
  expect(response).toHaveProperty("additionalTextConsent");
  expect(response).toHaveProperty("logo");
});

test("renders the terms and conditions modal", async () => {
  const userHash = "examplehash123";
  const initialUrl = `/consent/${userHash}`;
  render(
    <Routes>
      <Route path="/consent/:userHash" element={<Consent />} />
    </Routes>,
    {
      ...defaultCustomRenderOptions,
      initialUrl,
    }
  );

  const button = await screen.findByText(
    translationEN.consent.termsAndConditions
  );
  expect(button).toBeInTheDocument();
  expect(button.nodeName.toLowerCase()).toBe("button");

  userEvent.click(button);
  const iframe = await screen.findByTitle(/Terms and Conditions/i);
  expect(iframe).toBeInTheDocument();
  expect(iframe.nodeName.toLowerCase()).toBe("iframe");
});

test("renders the privacy policy modal", async () => {
  const userHash = "examplehash123";
  const initialUrl = `/consent/${userHash}`;
  render(
    <Routes>
      <Route path="/consent/:userHash" element={<Consent />} />
    </Routes>,
    {
      ...defaultCustomRenderOptions,
      initialUrl,
    }
  );

  const button = await screen.findByText(translationEN.consent.privacyPolicy);
  expect(button).toBeInTheDocument();
  expect(button.nodeName.toLowerCase()).toBe("button");

  userEvent.click(button);
  const iframe = await screen.findByTitle(/Privacy Policy/i);
  expect(iframe).toBeInTheDocument();
  expect(iframe.nodeName.toLowerCase()).toBe("iframe");
});

test("handles giving consent", async () => {
  const initialUrl = "/consent/examplehash123";
  const { mutationResponses } = render(
    <Routes>
      <Route path="/consent/:userHash" element={<Consent />} />
    </Routes>,
    {
      ...defaultCustomRenderOptions,
      initialUrl,
    }
  );

  userEvent.click(await screen.findByText(/^agree$/i));

  await waitFor(() => expect(window.location.pathname).toBe("/select-bank"));

  const { requestInput } = mutationResponses["consent"];
  expect(requestInput).toHaveProperty("consent", true);
});
test("handles refusing consent", async () => {
  const userHash = "examplehash123";
  const initialUrl = `/consent/${userHash}`;
  const { mutationResponses } = render(
    <Routes>
      <Route path="/consent/:userHash" element={<Consent />} />
    </Routes>,
    {
      ...defaultCustomRenderOptions,
      initialUrl,
    }
  );

  userEvent.click(await screen.findByText(/^disagree$/i));
  await waitFor(() =>
    expect(window.location.pathname).toBe(`/consent-refused/${userHash}`)
  );

  const { requestInput } = mutationResponses["consent"];
  expect(requestInput).toHaveProperty("consent", false);
});
