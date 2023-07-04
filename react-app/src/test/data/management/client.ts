import faker from "@faker-js/faker";

import type {
  ClientDetailsDTO,
  ClientsDetailsResponse,
} from "types/management/client";

//FIXME: temporary implementation, have to be reviewed when backend will be ready

export function getClient(): ClientDetailsDTO {
  return {
    id: faker.datatype.uuid(),
    creationDate: faker.date.recent().toISOString(),
    name: faker.company.companyName(),
    logo: "/mail/briqwise/original.png",
    defaultLanguage: "EN",
    additionalConsentText: faker.lorem.words(10),
    additionalReportText: faker.lorem.words(10),
    featureToggle: {
      overview: faker.datatype.boolean(),
      months: faker.datatype.boolean(),
      categories: faker.datatype.boolean(),
      apiToken: faker.datatype.boolean(),
      pDScoreFeature: faker.datatype.boolean(),
      signatureVerification: faker.datatype.boolean(),
    },
  };
}

export function getClients(count: number): ClientsDetailsResponse {
  const result = Array(count - 1)
    .fill(null)
    .map(() => getClient());
  result.push(getClient());
  return result;
}
