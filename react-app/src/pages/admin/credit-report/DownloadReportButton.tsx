import { useCallback } from "react";

import { Button } from "@yolt/design-system";
import { DateTime } from "luxon";
import { useTranslation } from "react-i18next";
import { useParams } from "react-router";

import { useGetDownloadCreditReport } from "api/admin/credit-report";

import { useCreditReport } from "./context/useCreditReport";

function maskIban(iban: string) {
  if (iban.length < 8) throw new Error("Iban number to short");

  return (
    iban.substring(0, 4) +
    "x".repeat(iban.length - 8) +
    iban?.substring(iban.length - 4)
  );
}

type Props = {
  disabled?: boolean;
};

export default function DownloadReportButton({ disabled }: Props) {
  const { t } = useTranslation();
  const { userId } = useParams<{ userId: string }>();
  const { mutateAsync: downloadReport, isLoading } =
    useGetDownloadCreditReport(userId);
  const { creditReportData } = useCreditReport();

  const onDownloadClicked = useCallback(async () => {
    const report = await downloadReport();

    if (!report || !creditReportData) return null;

    const a = document.createElement("a");
    a.href = URL.createObjectURL(
      new Blob([report.data], { type: "application/zip" })
    );

    const firstTransaction = creditReportData.adminReport.newestTransactionDate
      ? DateTime.fromISO(
          creditReportData.adminReport.newestTransactionDate
        ).toISODate()
      : null;
    const lastTransaction = creditReportData.adminReport.oldestTransactionDate
      ? DateTime.fromISO(
          creditReportData.adminReport.oldestTransactionDate
        ).toISODate()
      : null;
    const signature = creditReportData.signature
      ? creditReportData.signature
      : "NO-SIGNATURE";
    const subsig =
      signature.length > 16
        ? signature.substring(0, 7) +
          "_" +
          signature.substring(signature.length - 8)
        : "SIGNATURE-ERROR";

    const prefix = creditReportData.adminReport.iban
      ? maskIban(creditReportData.adminReport.iban)
      : "report";

    const fileName = `${prefix}_${firstTransaction}_${lastTransaction}_${subsig}.zip`;
    a.setAttribute("download", fileName);
    document.body.appendChild(a);
    a.click();
    document.body.removeChild(a);
  }, [creditReportData, downloadReport]);

  return (
    <Button
      isDisabled={disabled}
      isLoading={isLoading}
      onClick={onDownloadClicked}
      variant="primary"
    >
      {t("creditReport.downloadReport")}
    </Button>
  );
}
