import { useState } from "react";

import { Chakra, TableBlock } from "@yolt/design-system";

import { useGetAccountsList } from "api/management/clients";

import { DeleteAccountActionCell } from "./cells";

const { Box } = Chakra;

const columns = () => [
  {
    accessor: "id",
    Header: "id",
  },
  {
    accessor: "email",
    Header: "email",
  },
  {
    accessor: "provider",
    Header: "provider",
  },
  {
    id: "delete",
    accessor: "id",
    Cell: DeleteAccountActionCell,
  },
];

export default function AccountsTable() {
  const [currentPage, setCurrentPage] = useState<number>(0);
  const { data: result } = useGetAccountsList(currentPage);

  const translatedColumns = columns();

  return (
    <Box overflowY="auto" bg="white" paddingBottom="8px">
      <TableBlock<any>
        size="md"
        columns={translatedColumns}
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
    </Box>
  );
}
