import faker from "@faker-js/faker";

import type { Account } from "types/account";

export function getAccounts(): Account[] {
  return [
    {
      id: faker.datatype.uuid(),
      accountNumber: faker.finance.iban(),
      balance: faker.datatype.float({ max: 9999 }),
    },
    {
      id: faker.datatype.uuid(),
      accountNumber: faker.finance.iban(),
      balance: faker.datatype.float({ max: 9999 }),
    },
    {
      id: faker.datatype.uuid(),
      accountNumber: faker.finance.iban(),
      balance: faker.datatype.float({ max: 9999 }),
    },
    {
      id: faker.datatype.uuid(),
      accountNumber: faker.finance.iban(),
      balance: faker.datatype.float({ max: 9999 }),
    },
    {
      id: faker.datatype.uuid(),
      accountNumber: faker.finance.iban(),
      balance: faker.datatype.float({ max: 9999 }),
    },
  ];
}
