import { Button, Chakra, CurrencyCell, TableBlock } from "@yolt/design-system";
import { TFunction, useTranslation } from "react-i18next";

import { useAccounts, useSelectAccount } from "api/account";

import type { CellProps } from "react-table";

const { Center, Heading, Stack, Box } = Chakra;

function SelectCell<D extends object>({ value }: CellProps<D, string>) {
  const { t } = useTranslation();
  const { mutateAsync: selectAccount } = useSelectAccount();

  return (
    <Box marginLeft={-5}>
      <Button
        variant="primary"
        size="xs"
        onClick={async () => {
          await selectAccount(value);
        }}
      >
        {t("button.select")}
      </Button>
    </Box>
  );
}

const columns = (t: TFunction) => [
  {
    accessor: "accountNumber",
    Header: t("account.accountNumber") ?? "",
    minWidth: 300,
  },
  {
    accessor: "balance",
    Header: t("account.balance") ?? "",
    currency: "EUR",
    Cell: CurrencyCell,
  },
  {
    accessor: "id",
    Header: "",
    Cell: SelectCell,
  },
];

function SelectAccount() {
  const { t } = useTranslation();
  const { data: accounts } = useAccounts();

  if (!accounts) {
    return null;
  }

  const translatedColumns = columns(t);

  return (
    <Center as="main" display="flex" height="100%">
      <Stack
        display="flex"
        spacing="2rem"
        alignItems="center"
        justifyContent="center"
        padding="16px"
        margin="16px"
        bg="white"
      >
        <Heading as="h1">{t("account.select")}</Heading>

        <TableBlock<any>
          size="md"
          columns={translatedColumns}
          data={accounts ?? []}
          isLoading={!accounts}
        />
      </Stack>
    </Center>
  );
}

export default SelectAccount;
