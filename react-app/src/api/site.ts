import { PublicCreditScoringAPI } from "api/PublicCreditScoringAPI";
import { useAxiosMutation, useAxiosQuery } from "helpers/axios-query";

import type { Site } from "types/site";

export const QUERY_KEY = "banks";

export const useSites = () => {
  async function fetchSites() {
    const { data } = await PublicCreditScoringAPI.getSites();
    return data;
  }

  return useAxiosQuery(QUERY_KEY, fetchSites);
};

export const useConnectSite = () => {
  async function connectSite(siteId: Site["id"]) {
    return await PublicCreditScoringAPI.connectSite(siteId);
  }

  return useAxiosMutation(connectSite);
};

export const useConnectSiteCallback = (url: string) => {
  async function connectSiteCallback() {
    const { data } = await PublicCreditScoringAPI.connectSiteCallback(url);
    return data;
  }

  return useAxiosQuery(QUERY_KEY, connectSiteCallback);
};
