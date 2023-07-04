import { Button } from "@yolt/design-system";
import { useTranslation } from "react-i18next";

import { useRedirect } from "utils/route-utils";

import type { CellProps } from "react-table";

export function ViewClientActionCell<D extends object>({
  value,
}: CellProps<D, string>) {
  const { t } = useTranslation();
  const redirectToClient = useRedirect(`/management/client/${value}`);

  return (
    <>
      <Button
        size="xs"
        variant="primary"
        onClick={() => {
          redirectToClient();
        }}
      >
        {t("management.clientView")}
      </Button>
    </>
  );
}
