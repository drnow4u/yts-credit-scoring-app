import { ReactNode } from "react";

import { Chakra } from "@yolt/design-system";

const { Stack } = Chakra;

type Props = {
  children: ReactNode;
  margin: "xs" | "md";
};

export const Paragraph = ({ children, margin }: Props) => {
  const padding = margin === "xs" ? 5 : 10;
  return <Stack paddingX={padding}>{children}</Stack>;
};

export default Paragraph;
