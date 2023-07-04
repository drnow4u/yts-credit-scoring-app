import { useState } from "react";

import { Button, Chakra, DateCell, TableBlock } from "@yolt/design-system";
import { useTranslation } from "react-i18next";

import { useGetApiKeysList } from "api/admin/api-keys";

import CreateApiKeyModal from "components/modal/CreateApiKeyModal";

import RevokeCell from "./cells/RevokeCell";

const { Box, Text, useDisclosure } = Chakra;

const columns = [
  {
    accessor: "name",
    Header: "Name",
  },
  {
    accessor: "creationDate",
    Header: "Creation date",
    Cell: DateCell,
  },
  {
    accessor: "expiryDate",
    Header: "Expiry date",
    Cell: DateCell,
    maxWidth: 100,
  },
  {
    accessor: "lastUsed",
    Header: "Last used",
    Cell: DateCell,
  },
  {
    accessor: "status",
    Header: "Status",
    id: "action",
  },
  {
    accessor: "id",
    Header: "Actions",
    id: "remove",
    Cell: RevokeCell,
  },
];

function ApiSettingsTable() {
  const { t } = useTranslation();
  const [currentPage, setCurrentPage] = useState<number>(0);
  const { data: result } = useGetApiKeysList(currentPage);

  const {
    isOpen: isCreateKeyModalOpen,
    onOpen: onCreateApiKeyModalOpen,
    onClose: onCreateApiKeyModalClose,
  } = useDisclosure();

  return (
    <Box bg="white" paddingBottom="8px">
      <Text
        color="black"
        fontWeight={400}
        fontSize="xl"
        paddingInline={4}
        paddingTop={4}
      >
        {t("settings.tableTitle")}
      </Text>
      <TableBlock<any>
        size="md"
        columns={columns}
        data={result?.data ?? []}
        initialState={{
          pageSize: result?.paginationData.pageSize ?? 10,
          pageIndex: result?.paginationData.pageNumber ?? 0,
        }}
        isLoading={!result}
        isPaginated
        pageCount={result?.paginationData.totalPages ?? 1}
        setPageIndex={setCurrentPage}
      />
      <Box padding={4}>
        <Button onClick={onCreateApiKeyModalOpen} size="sm">
          {t("settings.AddNewToken")}
        </Button>
      </Box>
      <CreateApiKeyModal
        isOpen={isCreateKeyModalOpen}
        onClose={onCreateApiKeyModalClose}
      />
    </Box>
  );
}

export default ApiSettingsTable;
