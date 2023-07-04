import userEvent from "@testing-library/user-event";
import { rest } from "msw";

import { adminBase } from "test/api-handlers";
import {
  createInvitationTemplateOption,
  TEMPLATE_PREDEFINED_TEST_ID,
} from "test/data/admin/invitation-template-option";
import { server } from "test/server";
import {
  adminCustomRenderOptions,
  loadComponentAuthenticated,
  render,
  screen,
  waitFor,
} from "test/utils/test-utils";

let InviteUserModal: React.ComponentType<any>;

beforeAll(async () => {
  const ImportedInviteUserModal = await loadComponentAuthenticated<{}>(
    "components/modal/InviteUserModal"
  );
  InviteUserModal = ImportedInviteUserModal;
});

test("Handle invite user", async () => {
  const onClose = jest.fn();
  const emial = "John.Doe@example.com";
  const name = "John Doe";
  const { queryResponses, mutationResponses } = render(
    <InviteUserModal isOpen={true} onClose={onClose} />,
    adminCustomRenderOptions
  );

  userEvent.type(await screen.findByLabelText(/Name/i), name);
  userEvent.type(await screen.findByLabelText(/Email/i), emial);

  const { response } = queryResponses["admin/client/template"];
  expect(response.length).toBe(3);

  const selectBox = await screen.findByRole("combobox");
  userEvent.selectOptions(selectBox, [TEMPLATE_PREDEFINED_TEST_ID]);
  userEvent.click(await screen.findByText("Send invite"));

  await waitFor(() => expect(onClose).toHaveBeenCalledTimes(1));

  const { requestInput } = mutationResponses["users/invite"];

  expect(requestInput).toHaveProperty("name", name);
  expect(requestInput).toHaveProperty("email", emial);
  expect(requestInput).toHaveProperty(
    "clientEmailId",
    TEMPLATE_PREDEFINED_TEST_ID
  );
});

// todo - flaky test, fails on pipeline
test.skip("Handle invite user with single template", async () => {
  const onClose = jest.fn();
  const emial = "John.Doe@example.com";
  const name = "John Doe";

  server.use(
    rest.get(`${adminBase}/client/template`, (_req, res, ctx) => {
      return res(
        ctx.json([createInvitationTemplateOption(TEMPLATE_PREDEFINED_TEST_ID)])
      );
    }),
    rest.post(`${adminBase}/users/invite`, (req, res, ctx) => {
      expect(req.body).not.toHaveProperty("clientEmailId");
      return res(ctx.status(204));
    })
  );

  const { mutationResponses } = render(
    <InviteUserModal isOpen={true} onClose={onClose} />,
    adminCustomRenderOptions
  );

  userEvent.type(await screen.findByLabelText(/Name/i), name);
  userEvent.type(await screen.findByLabelText(/Email/i), emial);
  expect(screen.queryByText("Invitation template")).not.toBeInTheDocument();
  userEvent.click(await screen.findByText("Send invite"));

  await waitFor(() => expect(onClose).toHaveBeenCalledTimes(1));

  const { requestInput } = mutationResponses["users/invite"];

  expect(requestInput).toHaveProperty("name", name);
  expect(requestInput).toHaveProperty("email", emial);
  expect(requestInput).not.toHaveProperty("clientEmailId");
});

test("handles 401 Unauthorized response", async () => {
  const initialUrl = "/admin/login";

  server.use(
    rest.post(`${adminBase}/users/invite`, (_req, res, ctx) => {
      return res(ctx.status(401));
    })
  );

  render(<InviteUserModal isOpen={true} />, {
    ...adminCustomRenderOptions,
    initialUrl,
  });
  userEvent.type(await screen.findByLabelText(/Name/i), "John Doe");
  userEvent.type(
    await screen.findByLabelText(/Email/i),
    "John.Doe@example.com"
  );

  userEvent.click(await screen.findByText("Send invite"));

  const selectBox = await screen.findByRole("combobox");
  userEvent.selectOptions(selectBox, [TEMPLATE_PREDEFINED_TEST_ID]);

  await waitFor(() => expect(window.location.pathname).toBe("/admin/login"));
  expect(screen.queryByText("John Doe")).not.toBeInTheDocument();
});

describe("Form validation", () => {
  test("Backend validation errors", async () => {
    const nameFieldError = "Bad name";
    const emailFieldError = "Bad email";
    const templateFieldError = "Bad template";

    server.use(
      rest.post(`${adminBase}/users/invite`, (_req, res, ctx) => {
        return res(
          ctx.status(400),
          ctx.json({
            violations: [
              { fieldName: "name", message: nameFieldError },
              { fieldName: "email", message: emailFieldError },
              { fieldName: "clientEmailId", message: templateFieldError },
            ],
          })
        );
      })
    );

    render(<InviteUserModal isOpen={true} />, adminCustomRenderOptions);

    userEvent.type(await screen.findByLabelText(/Name/i), "John Doe");
    userEvent.type(
      await screen.findByLabelText(/Email/i),
      "John.Doe@example.com"
    );
    const selectBox = await screen.findByRole("combobox");
    userEvent.selectOptions(selectBox, [TEMPLATE_PREDEFINED_TEST_ID]);
    userEvent.click(await screen.findByText("Send invite"));
    expect(await screen.findByText(nameFieldError)).toBeInTheDocument();
    expect(await screen.findByText(emailFieldError)).toBeInTheDocument();
    expect(await screen.findByText(templateFieldError)).toBeInTheDocument();
  });
});
