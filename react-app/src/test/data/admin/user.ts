import faker from "@faker-js/faker";

import { InvitationStatus } from "helpers/invitation-status";

import type { UserDTO } from "types/admin/user";

interface MockUserOptions {
  email?: string;
  status?: InvitationStatus;
}

export function getUser(options?: MockUserOptions): UserDTO {
  return {
    userId: faker.datatype.uuid(),
    email: options?.email ?? faker.internet.email(),
    adminEmail: faker.internet.email(),
    status:
      options?.status ??
      faker.random.arrayElement(Object.values(InvitationStatus)),
    dateInvited: faker.date.recent().toISOString(),
    dateStatusUpdated: null,
    name: faker.name.findName(),
  };
}

export function getUsers(count: number) {
  const result = Array(count - 1)
    .fill(null)
    .map(() => getUser());
  result.push(getUser({ status: InvitationStatus.COMPLETED }));
  return result;
}
