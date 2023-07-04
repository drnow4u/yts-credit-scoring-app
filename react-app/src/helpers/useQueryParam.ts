import { useCallback } from "react";

import { useSearchParams } from "react-router-dom";

import type { NavigateOptions } from "react-router-dom";

export function useQueryParam<T>(
  key: string
): [T | undefined, (newQuery: T, options?: NavigateOptions) => void] {
  let [searchParams, setSearchParams] = useSearchParams();
  let paramValue = searchParams.get(key) as any;

  let setValue = useCallback(
    (newValue: T, options?: NavigateOptions) => {
      let newSearchParams = new URLSearchParams(searchParams);
      newSearchParams.set(key, newValue as any);
      setSearchParams(newSearchParams, options);
    },
    [key, searchParams, setSearchParams]
  );

  return [paramValue, setValue];
}

export default useQueryParam;
