import { ReactNode } from "react";

import { Chakra } from "@yolt/design-system";

const { Heading } = Chakra;

interface Props {
  children: ReactNode;
}

export const Title = ({ children }: Props) => {
  return (
    <Heading
      textColor="yolt.heroBlue"
      fontWeight={600}
      fontSize={{ base: "1xl", sm: "2xl", md: "2xl" }}
      width="70%"
      lineHeight={"110%"}
    >
      {children}
    </Heading>
  );
};

export default Title;
