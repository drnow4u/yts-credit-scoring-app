import faker from "@faker-js/faker";
import { InvitationTemplateOption } from "types/admin/invitation-template-option";

export const TEMPLATE_PREDEFINED_TEST_ID =
  "9c856ae9-42e3-4f25-aa90-2f61054c356c";

export function createInvitationTemplateOption(
  uuid: string = faker.datatype.uuid()
): InvitationTemplateOption {
  return {
    id: uuid,
    template: faker.random.word(),
  };
}

export function createInvitationTemplateOptions(
  count: number
): InvitationTemplateOption[] {
  const result = Array(count - 1)
    .fill(null)
    .map(() => createInvitationTemplateOption());
  result.push(createInvitationTemplateOption(TEMPLATE_PREDEFINED_TEST_ID));
  return result;
}
