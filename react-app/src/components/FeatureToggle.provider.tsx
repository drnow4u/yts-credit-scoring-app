import {
  createContext,
  FC,
  useContext,
  useEffect,
  useMemo,
  useState,
} from "react";

import parseFeatureKeys from "helpers/parseFeatureKeys";

type ContextDetails = {
  categories: boolean;
  months: boolean;
  overview: boolean;
  apiToken: boolean;
  signatureVerification: boolean;
  pDScoreFeature: boolean;
};

type ContextState = [
  ContextDetails,
  (details: Partial<ContextDetails>) => void
];

const FeatureToggleContext = createContext<ContextState>([
  {
    categories: false,
    months: false,
    overview: false,
    apiToken: false,
    signatureVerification: false,
    pDScoreFeature: false,
  },
  (details: Partial<ContextDetails>) => {},
]);

type Props = {
  pdscoreFeatureToggle?: boolean;
  signatureVerificationFeatureToggle?: boolean;
  categoryFeatureToggle?: boolean;
  monthsFeatureToggle?: boolean;
  overviewFeatureToggle?: boolean;
  apiTokenFeatureToggle?: boolean;
};

export const FeatureToggleProvider: FC<Props> = ({
  children,
  pdscoreFeatureToggle: pDScoreFeatureToggle = false,
  signatureVerificationFeatureToggle = false,
  categoryFeatureToggle = false,
  monthsFeatureToggle = false,
  overviewFeatureToggle = false,
  apiTokenFeatureToggle = false,
}) => {
  const contextValue = useMemo(
    () =>
      parseFeatureKeys({
        pdscoreFeatureToggle: pDScoreFeatureToggle,
        signatureVerificationFeatureToggle,
        categoryFeatureToggle,
        monthsFeatureToggle,
        overviewFeatureToggle,
        apiTokenFeatureToggle,
      }),
    [
      categoryFeatureToggle,
      monthsFeatureToggle,
      overviewFeatureToggle,
      pDScoreFeatureToggle,
      signatureVerificationFeatureToggle,
      apiTokenFeatureToggle,
    ]
  );

  const [mutableContextValue, setMutableContextValue] = useState(contextValue);

  useEffect(() => {
    setMutableContextValue(mutableContextValue);
  }, [contextValue, setMutableContextValue, mutableContextValue]);

  const setContext = (details: Partial<ContextDetails>) => {
    setMutableContextValue({
      ...mutableContextValue,
      ...details,
    });
  };

  return (
    <FeatureToggleContext.Provider value={[mutableContextValue, setContext]}>
      {children}
    </FeatureToggleContext.Provider>
  );
};

export function useFeatureToggle(): ContextState {
  const [featureToggle, setFeatureToggle] = useContext(FeatureToggleContext);
  return [featureToggle, setFeatureToggle];
}

type FeatureToggleWrapper = {
  toggle: keyof ContextDetails;
};

export const FeatureToggle: FC<FeatureToggleWrapper> = ({
  toggle,
  children,
}) => {
  const [{ [toggle]: displyToggle }] = useFeatureToggle();
  return <>{displyToggle && children}</>;
};
