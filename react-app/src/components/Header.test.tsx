import {
  adminCustomRenderOptions,
  loadComponentAuthenticated,
  render,
  screen,
} from "test/utils/test-utils";

let Header: React.ComponentType<any>;

beforeAll(async () => {
  const ImportedHeader = await loadComponentAuthenticated<{}>(
    "components/Header"
  );
  Header = ImportedHeader;
});

test("renders app title", async () => {
  render(<Header />, adminCustomRenderOptions);
  const headingElement = await screen.findByText(/Yolt Cashflow Analyser/i);
  expect(headingElement).toBeInTheDocument();
});
