import faker from "@faker-js/faker";

import { summaryBox, welcomeBox } from "./EmailTemplates";

import type { ClientEmailTemplatesDTO } from "types/management/client";

//FIXME: temporary implementation, have to be reviewed when backend will be ready

export function getEmail(): ClientEmailTemplatesDTO {
  return {
    id: faker.datatype.uuid(),
    title: faker.datatype.string(30),
    subTitle: faker.datatype.string(20),
    welcomeBox: welcomeBox,
    buttonText: "Submit",
    summaryBox: summaryBox,
    name: faker.company.companyName(),
  };
}

export function getEmails(count: number): ClientEmailTemplatesDTO[] {
  const result = Array(count - 1)
    .fill(null)
    .map(() => getEmail());
  result.push(getEmail());
  return result;
}
