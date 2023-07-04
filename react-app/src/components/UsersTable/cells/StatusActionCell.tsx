import { Button } from "@yolt/design-system";
import { useTranslation } from "react-i18next";
import { InvitationStatusString, User } from "types/admin/user";

import { useResendUserInvite } from "api/admin/user";

import { useRedirect } from "utils/route-utils";

import type { CellProps } from "react-table";

function StatusActionCell(props: CellProps<User>) {
  const { t } = useTranslation();
  const { mutate: resendUserInvite } = useResendUserInvite();
  const redirectToReport = useRedirect(
    `/admin/report/${props.row.original.userId}`
  );
  const status: InvitationStatusString = props.row.original.status;

  return (
    <>
      {status === "COMPLETED" && (
        <Button
          size="xs"
          variant="primary"
          onClick={() => {
            redirectToReport();
          }}
        >
          {t("userTable.view")}
        </Button>
      )}
      {status === "EXPIRED" && (
        <Button
          size="xs"
          onClick={() => {
            resendUserInvite(props.row.original.userId);
          }}
        >
          {t("userTable.resendInvite")}
        </Button>
      )}
    </>
  );
}

export default StatusActionCell;
