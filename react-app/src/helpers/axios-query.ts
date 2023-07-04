import {
  MutationFunction,
  QueryFunction,
  QueryKey,
  useMutation,
  UseMutationOptions,
  UseMutationResult,
  useQuery,
  UseQueryOptions,
  UseQueryResult,
} from "react-query";

import type { AxiosError } from "axios";

/**
 * This alias is a workaround for the fact that passing a generic in the following case is not possible in TS:
 * `Parameters<typeof useQuery<R, AxiosResponse<R>>>`
 *
 * Ideally we would use `useQuery` typings directly so this wouldn't be necessary, as it has the risk of becoming outdated.
 * That should however result in TS errors, so we would immediately be aware if it breaks.
 *
 * Note: As opposed to `useQuery`, this type only supports a single method signature.
 */
interface useQueryAlias<TQueryFnData, TError, TData = TQueryFnData> {
  (
    queryKey: QueryKey,
    queryFn: QueryFunction<TQueryFnData>,
    options?: UseQueryOptions<TQueryFnData, TError, TData>
  ): UseQueryResult<TData, TError>;
}

/**
 * This alias is a workaround for the fact that passing a generic in the following case is not possible in TS:
 * `Parameters<typeof useMutation<R, AxiosResponse<R>, TVariables>>`
 *
 * Ideally we would use `useMutation` typings directly so this wouldn't be necessary, as it has the risk of becoming outdated.
 * That should however result in TS errors, so we would immediately be aware if it breaks.
 *
 * Note: As opposed to `useMutation`, this type only supports a single method signature.
 */
interface useMutationAlias<
  TData,
  TError,
  TVariables = void,
  TContext = unknown
> {
  (
    mutationFn: MutationFunction<TData, TVariables>,
    options?: UseMutationOptions<TData, TError, TVariables, TContext>
  ): UseMutationResult<TData, TError, TVariables, TContext>;
}

/**
 * This hook is an alias/proxy to `useQuery`, but with improved error typing. It's basically the same as
 * `useQuery<R, AxiosResponse<R>>` where R will be inferred from the query function.
 * Using `useQuery` directly will only type the first generic correctly, leaving the Error type as `unknown`.
 */
export const useAxiosQuery = <R, S = R>(
  ...args: Parameters<useQueryAlias<R, AxiosError<R> | null, S>>
) => useQuery<R, AxiosError<R> | null, S>(...args);

/**
 * This hook is an alias/proxy to `useMutation`, but with improved error typing. It's basically the same as
 * `useMutation<R, AxiosResponse<R>, TVariables>` where R will be inferred from the mutation function.
 * Using `useMutation` directly will only type the first generic correctly, leaving the Error type as `unknown`.
 */
export const useAxiosMutation = <R, TVariables = void>(
  ...args: Parameters<useMutationAlias<R, AxiosError<R>, TVariables>>
) => useMutation<R, AxiosError<R>, TVariables>(...args);
