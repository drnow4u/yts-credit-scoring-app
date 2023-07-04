import {
  adminCustomRenderOptions,
  loadComponentAuthenticated,
  render,
  screen,
} from "test/utils/test-utils";

let UsersTable: React.ComponentType;

beforeAll(async () => {
  const ImportedTable = await loadComponentAuthenticated<{}>(
    "components/UsersTable/UsersTable"
  );
  UsersTable = ImportedTable;
});

test("render list of first 10 users", async () => {
  const { queryResponses } = render(<UsersTable />, adminCustomRenderOptions);

  // Including header cell
  const userAmount = 10 + 1;
  expect(await screen.findAllByRole("row")).toHaveLength(userAmount);

  const { response } = queryResponses["admin/users"];
  expect(response.result.length).toBe(userAmount - 1);
});
