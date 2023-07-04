import { Button } from "@yolt/design-system";
import { useTranslation } from "react-i18next";
import { ApiTokensDetails } from "types/admin/api-key";

import { useRevokeApiToken } from "api/admin/api-keys";
import useThrottle from "helpers/useThrottle";

import type { CellProps } from "react-table";

function RevokeCell(props: CellProps<ApiTokensDetails>) {
  const { status, id } = props.row.original;
  const { t } = useTranslation();

  const { mutate: revokeApiToken, isLoading } = useRevokeApiToken();
  const throttledRevokeApiToken = useThrottle(() => revokeApiToken(id));

  if (status === "ACTIVE") {
    return (
      <Button
        isDisabled={isLoading}
        variant="link"
        onClick={throttledRevokeApiToken}
      >
        {t("settings.revoke")}
      </Button>
    );
  } else return null;
}

export default RevokeCell;
