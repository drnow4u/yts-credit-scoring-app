import userEvent from "@testing-library/user-event";

import {
  loadComponentAuthenticated,
  render,
  screen,
  waitFor,
} from "test/utils/test-utils";

let SelectAccount: React.ComponentType<any>;

beforeAll(async () => {
  const ImportedSelectAccount = await loadComponentAuthenticated<{}>(
    "pages/SelectAccount"
  );
  SelectAccount = ImportedSelectAccount;
});

test("renders list of accounts", async () => {
  const { queryResponses } = render(<SelectAccount />);

  const accountsAmount = 5;
  expect((await screen.findAllByRole("button")).length).toBe(accountsAmount);
  expect(queryResponses?.account?.response.length).toBe(accountsAmount);
});

test("redirects after account selection", async () => {
  const { queryResponses, mutationResponses } = render(<SelectAccount />);

  userEvent.click((await screen.findAllByRole("button"))[0]);

  await waitFor(() =>
    expect(window.location.pathname).toBe("/cashflow-overview")
  );
  const { id } = queryResponses?.account?.response[0];
  const { requestInput } = mutationResponses["accounts/select"];

  expect(id).toBe(requestInput.id);
});
