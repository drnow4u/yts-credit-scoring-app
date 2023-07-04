import { useCallback } from "react";

import { useLocation, useNavigate } from "react-router-dom";

export const useRedirect = (url: string, replaceRoute = false) => {
  const navigate = useNavigate();
  const location = useLocation();

  return useCallback(() => {
    navigate(url, { replace: replaceRoute, state: location.state });
  }, [navigate, location.state, replaceRoute, url]);
};
