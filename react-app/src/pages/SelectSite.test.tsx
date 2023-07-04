import userEvent from "@testing-library/user-event";

import {
  connectSiteRedirectUrl,
  SITE_PREDEFINED_TEST_ID,
} from "test/data/site";
import {
  loadComponentAuthenticated,
  render,
  screen,
  waitFor,
} from "test/utils/test-utils";

let SelectSite: React.ComponentType;

beforeAll(async () => {
  const ImportedSelectSite = await loadComponentAuthenticated<{}>(
    "pages/SelectSite"
  );
  SelectSite = ImportedSelectSite;
});

test("renders list of sites", async () => {
  const { queryResponses } = render(<SelectSite />);

  const sitesAmount = 9;
  // One extra empty option for initial selection
  expect((await screen.findAllByRole("option")).length).toBe(sitesAmount + 1);

  expect(queryResponses?.banks?.response.length).toBe(sitesAmount);
});

test("redirects to bank on site selection", async () => {
  const mockLocationAssign = jest.fn();
  // without making a copy you will have a circular dependency problem during mocking
  const originalWindow = { ...window };

  jest.spyOn(window, "location", "get").mockImplementation(() => ({
    ...originalWindow.location,
    assign: mockLocationAssign,
  }));
  const { mutationResponses } = render(<SelectSite />);

  const selectBox = await screen.findByRole("combobox");

  userEvent.selectOptions(selectBox, [SITE_PREDEFINED_TEST_ID]);

  userEvent.click(await screen.findByRole("button", { name: /Next/i }));

  await waitFor(() => expect(mockLocationAssign).toHaveBeenCalledTimes(1));
  await waitFor(() =>
    expect(mockLocationAssign).toHaveBeenCalledWith(connectSiteRedirectUrl)
  );
  const { requestInput, response } = mutationResponses["sites/connect"];
  expect(requestInput).toHaveProperty("siteId", SITE_PREDEFINED_TEST_ID);
  expect(response).toHaveProperty("redirectUrl");
});
