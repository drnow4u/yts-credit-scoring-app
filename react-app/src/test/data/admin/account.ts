import faker from "@faker-js/faker";

import type { Account } from "types/admin/account";

export function getAccount(): Account {
  return {
    email: faker.internet.userName(),
    name: "ING",
    clientSettings: {
      defaultLanguage: "EN",
      categoryFeatureToggle: true,
      monthsFeatureToggle: true,
      overviewFeatureToggle: true,
      pdscoreFeatureToggle: true,
      signatureVerificationFeatureToggle: true,
      apiTokenFeatureToggle: true,
    },
  };
}
