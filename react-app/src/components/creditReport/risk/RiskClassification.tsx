import { Chakra } from "@yolt/design-system";
import { useTranslation } from "react-i18next";

import { pdGradeArray } from "helpers/creditReport";

import type { PDGrade } from "types/admin/creditReport";

const { Stack, Text } = Chakra;

interface RiskClassificationProps {
  pdGrade?: PDGrade | null;
}

function RiskClassification({ pdGrade }: RiskClassificationProps) {
  const { t } = useTranslation();
  return (
    <Stack
      bg="yolt.heroBlue"
      width="30%"
      alignItems="center"
      spacing={0}
      padding={4}
    >
      <Text color="white" fontSize="96" fontWeight="500">
        {pdGrade}
      </Text>
      <Stack
        isInline
        justifyContent="center"
        alignContent="center"
        width="100%"
        direction="row"
        spacing={2}
      >
        {pdGradeArray.map((grade) => (
          <Text
            key={grade}
            color={pdGrade === grade ? "white" : "yolt.lightGrey"}
            fontSize="sm"
            fontWeight="300"
          >
            {grade}
          </Text>
        ))}
      </Stack>
      <Text color="yolt.lightGrey" fontSize="sm" fontWeight="300">
        {t("report.pdscore.riskClassification")}
      </Text>
    </Stack>
  );
}

export { RiskClassification };
