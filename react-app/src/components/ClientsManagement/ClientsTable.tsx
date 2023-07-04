import { useState } from "react";

import { Chakra, ImageCell, TableBlock } from "@yolt/design-system";
import { TFunction, useTranslation } from "react-i18next";

import { useGetClientList } from "api/management/clients";

import { DeleteClientActionCell, ViewClientActionCell } from "./cells";

const { Box } = Chakra;

const columns = (t: TFunction) => [
  {
    accessor: "name",
    Header: t("management.clientName"),
  },
  {
    accessor: "logo",
    Header: "",
    Cell: ImageCell,
  },
  {
    id: "view",
    accessor: "id",
    Cell: ViewClientActionCell,
  },
  {
    id: "delete",
    accessor: "id",
    Cell: DeleteClientActionCell,
  },
];

export default function ClientsTable() {
  const [currentPage, setCurrentPage] = useState<number>(0);
  const { data: result } = useGetClientList(currentPage);
  const { t } = useTranslation();

  const translatedColumns = columns(t);

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
