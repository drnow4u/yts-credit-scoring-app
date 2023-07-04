import { ReactNode } from "react";

import { Chakra } from "@yolt/design-system";

import CircleIcon from "./CircleIcon";

const { Text: ChackraText } = Chakra;

type Props = {
  children: ReactNode;
  dot?: boolean;
  dotColor?: string;
  color?: string;
  fontWeight?: number;
  as?: string;
};

export const Text = ({
  children,
  dot,
  dotColor,
  fontWeight,
  color = "gray.500",
}: Props) => {
  return (
    <ChackraText fontWeight={fontWeight} color={color} maxW={"2xl"}>
      {dot && <CircleIcon color={dotColor} />} {children}
    </ChackraText>
  );
};

export default Text;
