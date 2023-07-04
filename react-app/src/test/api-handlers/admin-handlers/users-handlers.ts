import { rest } from "msw";

import { getUser, getUsers } from "test/data/admin/user";
import { readSessionStorage } from "test/utils/storage";

import { checkIfAuthenticated, TOKEN_HEADER_NAME } from "../common";
import { adminBase, IS_ADMIN_AUTHENTICATED_KEY } from "./common";

import type { User } from "types/admin/user";

const USERS_KEY = "users";

const usersHandlers = [
  rest.get(`${adminBase}/users`, async (req, res, ctx) => {
    const isAuthenticated = !!checkIfAuthenticated(
      IS_ADMIN_AUTHENTICATED_KEY,
      req.headers.get(TOKEN_HEADER_NAME)
    );
    if (!isAuthenticated) {
      return res(ctx.status(401));
    }
    let totalItems = 15;

    let currentUsers = readSessionStorage<User[]>(USERS_KEY, []);
    if (currentUsers.length < totalItems) {
      const newUsers = Array.from(getUsers(totalItems));
      sessionStorage.setItem(USERS_KEY, JSON.stringify(newUsers));
      currentUsers = readSessionStorage<User[]>(USERS_KEY);
    } else {
      totalItems = currentUsers.length;
    }

    const page = parseInt(req.url.searchParams.get("page") ?? "", 10);
    const pageSize = parseInt(req.url.searchParams.get("size") ?? "", 10);
    const pages = Math.ceil(totalItems / pageSize);

    const result = currentUsers.slice(page * pageSize, (page + 1) * pageSize);
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

  rest.delete(`${adminBase}/users/:userId`, async (req, res, ctx) => {
    const isAuthenticated = !!checkIfAuthenticated(
      IS_ADMIN_AUTHENTICATED_KEY,
      req.headers.get(TOKEN_HEADER_NAME)
    );
    if (!isAuthenticated) {
      return res(ctx.status(401));
    }

    const { userId } = req.params as { userId: string };
    try {
      const currentUsers = readSessionStorage<User[]>(USERS_KEY);
      if (!currentUsers) {
        return res(ctx.status(404));
      }

      const newUsers = currentUsers.filter((user) => user.userId !== userId);
      sessionStorage.setItem(USERS_KEY, JSON.stringify(newUsers));

      return res(ctx.status(204));
    } catch (error) {
      return res(ctx.status(404));
    }
  }),

  rest.put(
    `${adminBase}/users/:userId/resend-invite`,
    async (req, res, ctx) => {
      const isAuthenticated = !!checkIfAuthenticated(
        IS_ADMIN_AUTHENTICATED_KEY,
        req.headers.get(TOKEN_HEADER_NAME)
      );
      if (!isAuthenticated) {
        return res(ctx.status(401));
      }

      const { userId } = req.params as { userId: string };
      try {
        const currentUsers = readSessionStorage<User[]>(USERS_KEY);
        if (!currentUsers) {
          return res(ctx.status(404));
        }

        const modifiedUsers = [...currentUsers];

        const modifiedUser = modifiedUsers.find(
          (user) => user.userId === userId
        );
        if (modifiedUser) {
          modifiedUser.status = "INVITED";
        } else {
          return res(ctx.status(404));
        }

        sessionStorage.setItem(USERS_KEY, JSON.stringify(modifiedUsers));

        return res(ctx.status(200));
      } catch (error) {
        return res(ctx.status(404));
      }
    }
  ),

  rest.post(`${adminBase}/users/invite`, async (req, res, ctx) => {
    const isAuthenticated = !!checkIfAuthenticated(
      IS_ADMIN_AUTHENTICATED_KEY,
      req.headers.get(TOKEN_HEADER_NAME)
    );
    if (!isAuthenticated) {
      return res(ctx.status(401));
    }

    const { email } = req.body as { email: string };

    const currentUsers = readSessionStorage<User[]>(USERS_KEY, []);

    sessionStorage.setItem(
      USERS_KEY,
      JSON.stringify([...currentUsers, getUser({ email })])
    );

    return res(ctx.status(204));
  }),
];

export { usersHandlers };
