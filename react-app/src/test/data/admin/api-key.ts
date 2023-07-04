import faker from "@faker-js/faker";

import generateRandomText from "helpers/generateRandomText";

import type {
  ApiPermissionsResponse,
  CreateApiKeyResponse,
  GetApiTokensDetailsResponse,
} from "types/admin/api-key";

export function createApiToken(): CreateApiKeyResponse {
  return {
    clientToken: generateRandomText(2000),
  };
}

export function permissionsTokenList(): ApiPermissionsResponse {
  return ["INVITE_USER", "DOWNLOAD_REPORT"];
}

export function getTokenList(): GetApiTokensDetailsResponse {
  const arrayLenght = faker.random.number({ min: 1, max: 20 });
  const statuses = ["ACTIVE", "REVOKE"];
  return Array(arrayLenght)
    .fill(1)
    .map(() => ({
      id: faker.datatype.uuid(),
      name: faker.name.findName(),
      creationDate: faker.date.past(1).toISOString(),
      expiryDate: faker.date.recent(10).toISOString(),
      lastUsed: faker.date.past(1).toISOString(),
      status: statuses[
        faker.random.number({ min: 0, max: statuses.length - 1 })
      ] as "ACTIVE" | "REVOKE",
    }));
}
