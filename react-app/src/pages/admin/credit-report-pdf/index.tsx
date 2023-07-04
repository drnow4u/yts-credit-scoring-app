import CreditReportProvider from "../credit-report/context/credit-report.provider";
import { PDFView } from "./PDFView";

const CreditReportWrapper = () => {
  return (
    <CreditReportProvider>
      <PDFView />
    </CreditReportProvider>
  );
};

export default CreditReportWrapper;
