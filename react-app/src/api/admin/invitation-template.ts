import { useAxiosQuery } from "helpers/axios-query";

import { getInvitationTemplates } from "./creditScoringApi";

export const QUERY_KEY = "admin/client/template";

export const useInvitationTemplates = () => {
  async function fetchInvitationTemplates() {
    const { data } = await getInvitationTemplates();
    return data;
  }

  return useAxiosQuery(QUERY_KEY, fetchInvitationTemplates);
};
