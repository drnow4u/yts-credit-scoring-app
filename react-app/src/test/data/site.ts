import faker from "@faker-js/faker";

import { HOMEPAGE } from "helpers/constants";

import type { Site } from "types/site";

export const connectSiteRedirectUrl = `${HOMEPAGE}api/example_bank_login`;

export const SITE_PREDEFINED_TEST_ID = "9c856ae9-42e3-4f25-aa90-2f61054c356c";

export function getSites(): Site[] {
  return [
    {
      id: faker.datatype.uuid(),
      name: "ING",
    },
    {
      id: faker.datatype.uuid(),
      name: "ASN Bank",
    },
    {
      id: faker.datatype.uuid(),
      name: "bunq",
    },
    {
      id: faker.datatype.uuid(),
      name: "Knab",
    },
    {
      id: faker.datatype.uuid(),
      name: "Rabobank",
    },
    {
      id: faker.datatype.uuid(),
      name: "Triodos Bank",
    },
    {
      id: faker.datatype.uuid(),
      name: "Regiobank",
    },
    {
      id: SITE_PREDEFINED_TEST_ID,
      name: "ABN Amro",
    },
    {
      id: faker.datatype.uuid(),
      name: "SNS",
    },
  ];
}
