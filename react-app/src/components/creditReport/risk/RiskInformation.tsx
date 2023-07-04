import { Chakra, H4 } from "@yolt/design-system";
import { useTranslation } from "react-i18next";

const { Stack, Text, Spacer } = Chakra;

interface RiskInformationProps {
  lowerRate: number;
  upperRate: number | null;
}

function RiskInformation({ lowerRate, upperRate }: RiskInformationProps) {
  const { t } = useTranslation();
  return (
    <Stack width="70%" spacing={2} padding={4}>
      <H4 fontSize="96" fontWeight="500">
        {t("report.pdscore.title")}
      </H4>
      <Stack
        isInline
        alignContent="center"
        width="100%"
        direction="row"
        spacing={2}
      >
        <Text>{t("report.pdscore.expectedRateInfo")}</Text>
        {upperRate ? (
          <Text color="black">{`${lowerRate} - ${upperRate}`}</Text>
        ) : (
          <Text color="black">{`> ${lowerRate}`}</Text>
        )}
      </Stack>
      <Spacer />
      <Text fontWeight="300">{t("report.pdscore.riskNote")}</Text>
    </Stack>
  );
}

export { RiskInformation };
