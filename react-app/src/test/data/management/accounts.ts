import faker from "@faker-js/faker";

import type {
  AccountDetailsDTO,
  AccountsDetailsResponsee,
} from "types/management/client";

//FIXME: temporary implementation, have to be reviewed when backend will be ready

export function getAccount(): AccountDetailsDTO {
  return {
    id: faker.datatype.uuid(),
    creationDate: faker.date.recent().toISOString(),
    email: faker.internet.email(),
    provider: "Github",
  };
}

export function getAccounts(count: number): AccountsDetailsResponsee {
  const result = Array(count - 1)
    .fill(null)
    .map(() => getAccount());
  result.push(getAccount());
  return result;
}
