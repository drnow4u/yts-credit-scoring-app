import {
  adminCustomRenderOptions,
  loadComponentAuthenticated,
  render,
  screen,
} from "test/utils/test-utils";

let ApiSettingsTable: React.ComponentType;

beforeAll(async () => {
  const ImportedTable = await loadComponentAuthenticated<{}>(
    "components/ApiSettingsTable/ApiSettingsTable"
  );
  ApiSettingsTable = ImportedTable;
});

test("render list of first 10 api tokens", async () => {
  render(<ApiSettingsTable />, adminCustomRenderOptions);

  // Including header cell
  expect(await screen.findAllByRole("row")).toHaveLength(1);
});
