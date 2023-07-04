import { Chakra, Loading as YoltLoading } from "@yolt/design-system";
import { useTranslation } from "react-i18next";

const { Text } = Chakra;

interface LoadingProps {}

function Loading({ children = null }: React.PropsWithChildren<LoadingProps>) {
  const { t } = useTranslation();

  return (
    <YoltLoading>
      <Text>{t("loading")}</Text>
      {children}
    </YoltLoading>
  );
}

export default Loading;
