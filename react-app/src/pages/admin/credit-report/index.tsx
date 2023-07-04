import CreditReportProvider from "./context/credit-report.provider";
import CreditReport from "./CreditReport";

const CreditReportWrapper = () => {
  return (
    <CreditReportProvider>
      <CreditReport />
    </CreditReportProvider>
  );
};

export default CreditReportWrapper;
