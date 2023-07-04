import { useEffect } from "react";

import { useErrorHandler } from "react-error-boundary";
import { useQueryClient } from "react-query";

import { isAxiosError } from "utils/type-utils";

interface RegisterQueryClientErrorHandlerProps {
  children: JSX.Element[] | JSX.Element;
}
function RegisterQueryClientErrorHandler({
  children,
}: RegisterQueryClientErrorHandlerProps) {
  const queryClient = useQueryClient();
  const handleError = useErrorHandler();

  useEffect(() => {
    let isCancelled = false;

    function onError(error: unknown) {
      if (
        isAxiosError(error) &&
        error.response?.status === 401 &&
        !window.location.pathname.includes("/login") &&
        !isCancelled
      ) {
        return handleError(error);
      }
    }

    const defaultOptions = queryClient.getDefaultOptions();

    queryClient.setDefaultOptions({
      queries: { ...defaultOptions.queries, onError },
      mutations: { ...defaultOptions.mutations, onError },
    });

    return () => {
      isCancelled = true;
    };
  }, [queryClient, handleError]);

  return <>{children}</>;
}

export default RegisterQueryClientErrorHandler;
