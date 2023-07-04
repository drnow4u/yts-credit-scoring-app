import {
  createCreditScoreResult,
  getCreditReport,
} from "test/data/credit-report";

import { buildStringForSignatureVerification } from "./security";

describe("jsonpath value extraction", () => {
  test("extract values from credit report using jsonpath", () => {
    const fields = [
      "$['iban']",
      "$['initialBalance']",
      "$['newestTransactionDate']",
      "$['oldestTransactionDate']",
      "$['creditLimit']",
      "$['transactionsSize']",
      "$['accountHolder']",
      "$['creditScoreMonthly'][0]['month']",
      "$['creditScoreMonthly'][0]['year']",
      "$['creditScoreMonthly'][0]['highestBalance']",
      "$['creditScoreMonthly'][0]['lowestBalance']",
      "$['creditScoreMonthly'][0]['totalIncoming']",
      "$['creditScoreMonthly'][0]['totalOutgoing']",
      "$['creditScoreMonthly'][1]['month']",
      "$['creditScoreMonthly'][1]['year']",
      "$['creditScoreMonthly'][1]['highestBalance']",
      "$['creditScoreMonthly'][1]['lowestBalance']",
      "$['creditScoreMonthly'][1]['totalIncoming']",
      "$['creditScoreMonthly'][1]['totalOutgoing']",
    ];
    const creditScoreMonthly = [
      createCreditScoreResult(1, {
        year: 2021,
        highestBalance: "9999.90",
        lowestBalance: "0.00",
        averageBalance: "1000.01",
        totalIncoming: "4567.66",
        totalOutgoing: "6789.23",
      }),
      createCreditScoreResult(2, {
        year: 2021,
        highestBalance: "12345.00",
        lowestBalance: "-1.10",
        averageBalance: "990.91",
        totalIncoming: "10000.00",
        totalOutgoing: "4098.45",
      }),
    ];
    // given
    const report = getCreditReport({
      accountHolder: "some name",
      iban: "PL12345678899",
      initialBalance: "123.45",
      creditLimit: "2000.00",
      creditScoreMonthly: creditScoreMonthly,
      newestTransactionDate: "2021-01-02",
      oldestTransactionDate: "2021-02-28",
      transactionsSize: 87,
    });
    const expected =
      "PL12345678899;123.45;2021-01-02;2021-02-28;2000.00;87;some name;1;2021;9999.90;0.00;4567.66;6789.23;2;2021;12345.00;-1.10;10000.00;4098.45;";
    // when
    const received = buildStringForSignatureVerification(report, fields);
    // then
    expect(received).toEqual(expected);

    // given
    const report2 = getCreditReport({
      accountHolder: "ᚠᚢᚦᚨᚱᛗᛞ name",
      iban: "EU12345678899",
      initialBalance: "123.45",
      creditLimit: "2000.00",
      creditScoreMonthly: creditScoreMonthly,
      newestTransactionDate: "2021-01-02",
      oldestTransactionDate: "2021-02-28",
      transactionsSize: 87,
    });
    const expected2 =
      "EU12345678899;123.45;2021-01-02;2021-02-28;2000.00;87;ᚠᚢᚦᚨᚱᛗᛞ name;1;2021;9999.90;0.00;4567.66;6789.23;2;2021;12345.00;-1.10;10000.00;4098.45;";
    // when
    const received2 = buildStringForSignatureVerification(report2, fields);
    // then
    expect(received2).toEqual(expected2);

    // given
    const report3 = getCreditReport({
      accountHolder: "かくて勇者は旅立たん",
      iban: "PL12345678899",
      initialBalance: "123.45",
      creditLimit: "2000.00",
      creditScoreMonthly: creditScoreMonthly,
      newestTransactionDate: "2021-01-02",
      oldestTransactionDate: "2021-02-28",
      transactionsSize: 87,
    });
    const expected3 =
      "PL12345678899;123.45;2021-01-02;2021-02-28;2000.00;87;かくて勇者は旅立たん;1;2021;9999.90;0.00;4567.66;6789.23;2;2021;12345.00;-1.10;10000.00;4098.45;";
    // when
    const received3 = buildStringForSignatureVerification(report3, fields);
    // then
    expect(received3).toEqual(expected3);
  });
});
