import { FC, useCallback, useEffect, useState } from "react";

import { Chakra } from "@yolt/design-system";
import html2canvas from "html2canvas";
import jsPDF from "jspdf";
import { useTranslation } from "react-i18next";
import { useParams } from "react-router";
import { Navigate } from "react-router-dom";

import {
  CreditReportCategoriesTables,
  CreditReportDashboard,
  CreditReportOverview,
  CreditReportTable,
} from "components/creditReport";
import {
  FeatureToggle,
  useFeatureToggle,
} from "components/FeatureToggle.provider";
import Loading from "components/Loading";

import { useCreditReport } from "../credit-report/context/useCreditReport";

import "./pdf-styles.scss";

const { Box, Text, Flex } = Chakra;

export enum pdfElement {
  details = "details-pdf",
  overview = "overview-pdf",
  monthly = "monthly-pdf",
  categories = "categories-pdf",
}

export const PDFView: FC = () => {
  const [isGeneratingPdf, setIsGeneratingPdf] = useState(false);
  const [generatedPdfUrl, setGeneratedPdfUrl] = useState<string>();
  const { t } = useTranslation();
  const { userId } = useParams<{ userId: string }>();
  const [{ months, overview, categories }] = useFeatureToggle();
  const {
    creditReportData: creditReport,
    isFetchingCreditReport,
    creditReportOverviewData: creditReportOverview,
    isFetchingOverview,
    creditReportMonthlyData: creditReportMonthly,
    isFetchingMonths,
  } = useCreditReport();

  const genneratePdf = useCallback(async () => {
    if (!creditReport) return null;
    const pdf = new jsPDF();
    await generateImage(pdfElement.details);
    const detailsImage = await generateImage(pdfElement.details);
    const { width: pdfDetailsWidth, height: pdfDetailsHeight } =
      calculateComponentSize(pdf, detailsImage);
    pdf.addImage(detailsImage, "JPEG", 0, 0, pdfDetailsWidth, pdfDetailsHeight);

    const marginComponentFromDetails = pdfDetailsHeight + 3;

    if (overview) {
      const overviewImage = await generateImage(pdfElement.overview);
      const { width: pdfOverviewWidth, height: pdfOverviewHeight } =
        calculateComponentSize(pdf, overviewImage);
      pdf.addImage(
        overviewImage,
        "JPEG",
        0,
        marginComponentFromDetails,
        pdfOverviewWidth,
        pdfOverviewHeight
      );
      (months || categories) && pdf.addPage();
    }

    if (months) {
      const monthlyImage = await generateImage(pdfElement.monthly);
      const startFrom = overview ? 0 : marginComponentFromDetails;
      const { width: pdfMonthsWidth, height: pdfMonthsHeight } =
        calculateComponentSize(pdf, monthlyImage);
      pdf.addImage(
        monthlyImage,
        "JPEG",
        0,
        startFrom,
        pdfMonthsWidth,
        pdfMonthsHeight
      );
      categories && pdf.addPage();
    }

    if (categories) {
      const startFrom = overview || months ? 0 : marginComponentFromDetails;
      const categoriesImage = await generateImage(pdfElement.categories);
      const { width: pdfcategoriesWidth, height: pdfcategoriesHeight } =
        calculateComponentSize(pdf, categoriesImage);
      pdf.addImage(
        categoriesImage,
        "JPEG",
        0,
        startFrom,
        pdfcategoriesWidth,
        pdfcategoriesHeight
      );
    }

    setIsGeneratingPdf(false);
    const blobPDF = new Blob([pdf.output("blob")], {
      type: "application/pdf",
    });

    const blobUrl = URL.createObjectURL(blobPDF);
    setGeneratedPdfUrl(blobUrl);
  }, [creditReport, categories, overview, months]);

  useEffect(() => {
    if (
      !creditReport ||
      (!creditReportMonthly && months) ||
      (!creditReportOverview && overview) ||
      isFetchingOverview ||
      isFetchingCreditReport ||
      isFetchingMonths
    )
      return;
    setIsGeneratingPdf(true);
    genneratePdf();
  }, [
    genneratePdf,
    userId,
    creditReport,
    creditReportMonthly,
    months,
    creditReportOverview,
    overview,
    isFetchingOverview,
    isFetchingCreditReport,
    isFetchingMonths,
  ]);

  if (
    !creditReport ||
    (!creditReportMonthly && months) ||
    (!creditReportOverview && overview) ||
    isFetchingOverview ||
    isFetchingCreditReport ||
    isFetchingMonths
  ) {
    return <Loading />;
  }

  const generateImage = async (elementId: string) => {
    const domElement: any = document.querySelector(`#${elementId}`);
    const canvansElement = await html2canvas(domElement, {
      windowWidth: 1500,
    });
    return canvansElement.toDataURL("image/jpeg");
  };

  const calculateComponentSize = (
    pdf: jsPDF,
    imageData: string | HTMLCanvasElement | HTMLImageElement | Uint8Array
  ) => {
    const imageProperties = pdf.getImageProperties(imageData);
    const width = pdf.internal.pageSize.getWidth();
    const height = (imageProperties.height * width) / imageProperties.width;
    return {
      width,
      height,
    };
  };

  if (!userId) {
    return <Navigate to="admin" replace />;
  }

  if (generatedPdfUrl) {
    return (
      <Box zIndex="99" position="absolute" width="100vw" height="100vh">
        <iframe
          width="100%"
          height="100%"
          style={{ overflow: "scroll" }}
          title="report-pdf"
          src={generatedPdfUrl}
        />
      </Box>
    );
  }

  return (
    <Flex
      display="flex"
      align-items="center"
      justify-content="center"
      backgroundColor="white"
    >
      {isGeneratingPdf && (
        <Box position="relative">
          <Box
            marginTop="10%"
            width="50%"
            height="20%"
            zIndex={99}
            left={0}
            right={0}
            top={0}
            bottom={0}
            marginLeft="auto"
            marginRight="auto"
            position="fixed"
            shadow="base"
          >
            <Box padding={2} backgroundColor="white">
              <Loading />
            </Box>
          </Box>
        </Box>
      )}
      <Box id="pdf-container" width={1500}>
        <Box width={1500} id={pdfElement.details} marginBottom={10}>
          <Box padding={5}>
            <CreditReportOverview
              creditReport={creditReport.adminReport}
              userEmail={creditReport.userEmail}
            />
          </Box>
        </Box>
        <FeatureToggle toggle="overview">
          <Box width={1500} id={pdfElement.overview}>
            <Box padding={5}>
              {creditReportMonthly && creditReportOverview && (
                <CreditReportDashboard
                  creditScoreMonthly={creditReportMonthly}
                  creditScoreOverviewResponse={creditReportOverview}
                  creditScoreResponse={creditReport}
                  animationChartActive={false}
                />
              )}
            </Box>
          </Box>
        </FeatureToggle>
        <FeatureToggle toggle="months">
          <Box width={1500} id={pdfElement.monthly}>
            {creditReportMonthly && (
              <Box padding={5}>
                <Text
                  as="h1"
                  fontSize="xl"
                  fontWeight="600"
                  textColor="yolt.heroBlue"
                  marginBottom={5}
                >
                  {t("general.months")}
                </Text>
                <CreditReportTable
                  creditScoreMonthly={creditReportMonthly}
                  creditReport={creditReport.adminReport}
                />
              </Box>
            )}
          </Box>
        </FeatureToggle>

        <FeatureToggle toggle="categories">
          <Box width={1500} id={pdfElement.categories}>
            <Box padding={5}>
              <Text
                as="h1"
                fontSize="xl"
                fontWeight="600"
                textColor="yolt.heroBlue"
                marginBottom={5}
              >
                {t("general.categories")}
              </Text>
              <CreditReportCategoriesTables
                currency={creditReport.adminReport.currency}
                userId={userId}
              />
            </Box>
          </Box>
        </FeatureToggle>
      </Box>
    </Flex>
  );
};
