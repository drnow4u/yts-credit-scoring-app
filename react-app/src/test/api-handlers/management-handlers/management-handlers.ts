import { rest } from "msw";
import {
  AccountDetails,
  AccountDetailsDTO,
  AccountsDetailsResponsee,
  ClientCreateDTO,
  ClientDetails,
  ClientEmailTemplatesDTO,
  ClientsDetailsResponse,
  ClientUpdateDTO,
  ClientUpdateFeatureToggle,
} from "types/management/client";

import { getAccount, getAccounts } from "test/data/management/accounts";
import { getClient, getClients } from "test/data/management/client";
import { getEmail, getEmails } from "test/data/management/email";
import { readSessionStorage } from "test/utils/storage";

import { managementBase } from "./common";

//FIXME: temporary implementation, have to be reviewed when backend will be ready

const CLIENTS_KEY = "clients";
const ACCOUNTS_KEY = "accounts";

const managementHandlers = [
  rest.get(`${managementBase}/clients`, async (req, res, ctx) => {
    let totalItems = 15;

    let currentClients = readSessionStorage<ClientsDetailsResponse>(
      CLIENTS_KEY,
      []
    );
    if (currentClients.length < totalItems) {
      const newClients = Array.from(getClients(totalItems));
      sessionStorage.setItem(CLIENTS_KEY, JSON.stringify(newClients));
      currentClients = readSessionStorage<ClientsDetailsResponse>(CLIENTS_KEY);
    } else {
      totalItems = currentClients.length;
    }

    const page = parseInt(req.url.searchParams.get("page") ?? "", 10);
    const pageSize = parseInt(req.url.searchParams.get("size") ?? "", 10);
    const pages = Math.ceil(totalItems / pageSize);

    const result = currentClients.slice(page * pageSize, (page + 1) * pageSize);
    return res(
      ctx.json(result),
      ctx.set({
        "X-Pagination-Page": page.toString(),
        "X-Pagination-Pages": pages.toString(),
        "X-Pagination-PageSize": pageSize.toString(),
        "X-Total-Count": totalItems.toString(),
      })
    );
  }),
  rest.delete(`${managementBase}/clients/:clientId`, async (req, res, ctx) => {
    const { clientId } = req.params as { clientId: ClientDetails["id"] };
    try {
      const currentClients =
        readSessionStorage<ClientsDetailsResponse>(CLIENTS_KEY);
      if (!currentClients) {
        return res(ctx.status(404));
      }

      const newClients = currentClients.filter(
        (client) => client.id !== clientId
      );
      sessionStorage.setItem(CLIENTS_KEY, JSON.stringify(newClients));

      return res(ctx.status(204));
    } catch (error) {
      return res(ctx.status(404));
    }
  }),

  rest.get(`${managementBase}/accounts`, async (req, res, ctx) => {
    let totalItems = 15;
    let currentAccounts = readSessionStorage<AccountsDetailsResponsee>(
      ACCOUNTS_KEY,
      []
    );
    if (currentAccounts.length < totalItems) {
      const newAccounts = Array.from(getAccounts(totalItems));
      sessionStorage.setItem(ACCOUNTS_KEY, JSON.stringify(newAccounts));
      currentAccounts =
        readSessionStorage<AccountsDetailsResponsee>(ACCOUNTS_KEY);
    } else {
      totalItems = currentAccounts.length;
    }

    const page = parseInt(req.url.searchParams.get("page") ?? "", 10);
    const pageSize = parseInt(req.url.searchParams.get("size") ?? "", 10);
    const pages = Math.ceil(totalItems / pageSize);

    const result = currentAccounts.slice(
      page * pageSize,
      (page + 1) * pageSize
    );
    return res(
      ctx.json(result),
      ctx.set({
        "X-Pagination-Page": page.toString(),
        "X-Pagination-Pages": pages.toString(),
        "X-Pagination-PageSize": pageSize.toString(),
        "X-Total-Count": totalItems.toString(),
      })
    );
  }),
  rest.delete(`${managementBase}/account/:accountId`, async (req, res, ctx) => {
    const { accountId } = req.params as { accountId: AccountDetails["id"] };
    try {
      const currentClients =
        readSessionStorage<ClientsDetailsResponse>(ACCOUNTS_KEY);
      if (!currentClients) {
        return res(ctx.status(404));
      }

      const newAccount = currentClients.filter(
        (client) => client.id !== accountId
      );
      sessionStorage.setItem(ACCOUNTS_KEY, JSON.stringify(newAccount));

      return res(ctx.status(204));
    } catch (error) {
      return res(ctx.status(404));
    }
  }),
  rest.post(`${managementBase}/account`, async (req, res, ctx) => {
    let currentClients = readSessionStorage<ClientsDetailsResponse>(
      ACCOUNTS_KEY,
      []
    );

    const body = req.body as AccountDetailsDTO;

    const newClient = {
      ...getAccount(),
      ...body,
      id: getAccount().id,
    };

    sessionStorage.setItem(
      ACCOUNTS_KEY,
      JSON.stringify([...currentClients, newClient])
    );

    currentClients = readSessionStorage<ClientsDetailsResponse>(ACCOUNTS_KEY);

    return res(ctx.status(200), ctx.json(newClient));
  }),
  rest.post(`${managementBase}/client`, async (req, res, ctx) => {
    let currentClients = readSessionStorage<ClientsDetailsResponse>(
      CLIENTS_KEY,
      []
    );

    const body = req.body as ClientCreateDTO;

    const newClient = {
      ...getClient(),
      ...body,
    };

    sessionStorage.setItem(
      CLIENTS_KEY,
      JSON.stringify([...currentClients, newClient])
    );

    currentClients = readSessionStorage<ClientsDetailsResponse>(CLIENTS_KEY);

    return res(ctx.status(200), ctx.json(newClient));
  }),

  rest.get(`${managementBase}/client/:clientId`, async (req, res, ctx) => {
    return res(ctx.status(200), ctx.json(getClient()));
  }),
  rest.put(`${managementBase}/client`, async (req, res, ctx) => {
    const body = req.body as ClientUpdateDTO;

    const newClient = {
      ...getClient(),
      ...body,
    };

    return res(ctx.status(200), ctx.json(newClient));
  }),

  rest.put(`${managementBase}/client/toggle`, async (req, res, ctx) => {
    const { id, ...body } = req.body as ClientUpdateFeatureToggle;

    const newClient = {
      ...getClient(),
      featureToggle: {
        ...body,
      },
    };

    return res(ctx.status(200), ctx.json(newClient));
  }),
  rest.get(
    `${managementBase}/client/:clientId/email-templates`,
    async (req, res, ctx) => {
      return res(ctx.status(200), ctx.json(getEmails(5)));
    }
  ),
  rest.post(
    `${managementBase}/client/:clientId/email-template`,
    async (req, res, ctx) => {
      const body = req.body as ClientEmailTemplatesDTO;

      const newEmail = {
        ...getEmail(),
        ...body,
      };

      return res(ctx.status(200), ctx.json(newEmail));
    }
  ),

  rest.put(
    `${managementBase}/client/:clientId/email-template`,
    async (req, res, ctx) => {
      const body = req.body as ClientEmailTemplatesDTO;

      const editedEmail = {
        ...getEmail(),
        ...body,
      };

      return res(ctx.status(200), ctx.json(editedEmail));
    }
  ),
  rest.post(
    `${managementBase}/client/:clientId/logo`,
    async (req, res, ctx) => {
      return res(ctx.status(204));
    }
  ),
];

export { managementBase, managementHandlers };
