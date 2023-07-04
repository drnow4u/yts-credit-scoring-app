import { Chakra } from "@yolt/design-system";
import { useTranslation } from "react-i18next";
import { RiskClassificationResponse } from "types/admin/creditReport";

import { RiskClassification } from "./RiskClassification";
import { RiskInformation } from "./RiskInformation";

const { Stack, Text } = Chakra;

interface CreditReportRiskProps {
  riskClassification: RiskClassificationResponse;
}

function CreditReportRisk({ riskClassification }: CreditReportRiskProps) {
  const { t } = useTranslation();
  return (
    <Stack
      bg="white"
      width={{
        base: "100%",
        xl: "50%",
        "2xl": "40%",
        "3xl": "30%",
      }}
      spacing={2}
      direction="row"
    >
      {riskClassification.status === "COMPLETED" && (
        <>
          <RiskClassification pdGrade={riskClassification.grade} />
          <RiskInformation
            lowerRate={riskClassification.rateLower}
            upperRate={riskClassification.rateUpper}
          />
        </>
      )}
      {riskClassification.status === "ERROR" && (
        <Text
          fontSize="x-large"
          color="red.500"
          width="100%"
          align="center"
          pt={150}
        >
          {t("report.pdscore.error")}
        </Text>
      )}
      {riskClassification.status === "ERROR_NOT_ENOUGH_TRANSACTIONS" && (
        <Text
          fontSize="x-large"
          color="red.500"
          width="100%"
          align="center"
          pt={150}
        >
          {t("report.pdscore.notEnoughAmountError")}
        </Text>
      )}
    </Stack>
  );
}

export default CreditReportRisk;
