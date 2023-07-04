import { useMemo, useRef } from "react";

export const getCurrencyFormatter = (
  currency: string,
  language: string,
  options?: Partial<Intl.NumberFormatOptions>
) =>
  new Intl.NumberFormat(language, {
    style: "currency",
    currency,
    ...options,
  });

export const useCurrencyFormatter = (
  currency: string,
  language: string = navigator.language,
  options?: Partial<Intl.NumberFormatOptions>
) => {
  const CurrencyFormatter = useRef<Intl.NumberFormat>(
    getCurrencyFormatter(currency, language, options)
  );

  return CurrencyFormatter;
};

export const getDateTimeFormatter = (
  language: string,
  options?: Partial<Intl.DateTimeFormatOptions>
) => new Intl.DateTimeFormat(language, options);

export const useDateTimeFormatter = (
  language: string = navigator.language,
  options?: Partial<Intl.DateTimeFormatOptions>
) => {
  const DateTimeFormatter = useMemo(
    () => getDateTimeFormatter(language, options),
    [language, options]
  );

  return DateTimeFormatter;
};
