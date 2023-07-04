import { useEffect, useState } from "react";

import { Chakra, DateCell, TableBlock } from "@yolt/design-system";
import { TFunction, useTranslation } from "react-i18next";

import { useUsers } from "api/admin/user";
import { USERS_TABLE_POLLING_INTERVAL } from "helpers/constants";
import { useQueryParam } from "helpers/useQueryParam";

import DeleteActionCell from "./cells/DeleteActionCell";
import StatusActionCell from "./cells/StatusActionCell";
import TextWithTooltipCell from "./cells/TextWithTooltipCell";

const { Box } = Chakra;

const columns = (t: TFunction) => [
  {
    accessor: "email",
    Header: t("general.email"),
  },
  {
    accessor: "adminEmail",
    Header: t("userTable.adminEmail"),
  },
  { accessor: "name", Header: t("general.userName"), maxWidth: 90 },
  {
    accessor: "dateInvited",
    Header: t("userTable.dateInvited"),
    Cell: DateCell,
  },
  {
    accessor: "status",
    Header: t("userTable.invitationStatus"),
    Cell: TextWithTooltipCell,
  },
  {
    accessor: "status",
    Header: "",
    id: "action",
    Cell: StatusActionCell,
  },
  {
    accessor: "userId",
    Header: "",
    id: "remove",
    maxWidth: 40,
    Cell: DeleteActionCell,
  },
];

function UsersTable() {
  const [initTable, setInitTable] = useState(false);
  const [pageParam, setPageParam] = useQueryParam<number>("page");
  const [currentPage, setCurrentPage] = useState<number>(0);
  const [pageValidator, setPageValidator] = useState(0);

  let { data: result } = useUsers({
    page: currentPage,
    refetchInterval: USERS_TABLE_POLLING_INTERVAL,
  });
  const { t } = useTranslation();
  const translatedColumns = columns(t);

  useEffect(() => {
    if (!pageParam) {
      setPageParam(1);
      setCurrentPage(0);
      return;
    }

    if (pageParam) {
      setCurrentPage(+pageParam - 1);
    }
    setInitTable(true);
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [pageParam]);

  useEffect(() => {
    if (initTable) setPageParam(pageValidator + 1);
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [pageValidator]);

  return (
    <Box overflowY="auto" bg="white" paddingBottom="8px">
      <TableBlock<any>
        size="md"
        columns={translatedColumns}
        data={result?.result ?? []}
        initialState={{
          pageSize: result?.paginationData.pageSize ?? 10,
          pageIndex: result?.paginationData.pageNumber ?? 0,
        }}
        isLoading={!result}
        isPaginated
        pageCount={result?.paginationData.totalPages ?? 1}
        setPageIndex={setPageValidator}
      />
    </Box>
  );
}

export default UsersTable;
