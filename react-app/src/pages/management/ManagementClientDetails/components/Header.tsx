import { Chakra } from "@yolt/design-system";

const { Flex, Text } = Chakra;

type HeaderProps = {
  logo: string;
  name: string;
};

const Header = ({ logo, name }: HeaderProps) => {
  return (
    <Flex
      marginTop={4}
      direction={{ sm: "column", md: "row" }}
      mx="1.5rem"
      maxH="330px"
      w={{ sm: "90%", xl: "95%" }}
      justifyContent={{ sm: "center", md: "space-between" }}
      align="center"
      backdropFilter="saturate(200%) blur(50px)"
      boxShadow="0px 2px 5.5px rgba(0, 0, 0, 0.02)"
      border="2px solid"
      borderColor="white"
      p="24px"
      borderRadius="20px"
    >
      <Flex
        mb={{ sm: "10px", md: "0px" }}
        direction={{ sm: "column", md: "row" }}
        w={{ sm: "100%" }}
        textAlign={{ sm: "center", md: "start" }}
      >
        <Flex me={{ md: "22px" }} w="80px" h="80px" borderRadius="15px">
          <Flex alignSelf="center" as="img" src={logo} />
        </Flex>
        <Flex direction="column" maxWidth="100%" my={{ sm: "14px" }}>
          <Text
            fontSize={{ sm: "lg", lg: "xl" }}
            fontWeight="bold"
            ms={{ sm: "8px", md: "0px" }}
          >
            {name}
          </Text>
        </Flex>
      </Flex>
    </Flex>
  );
};

export default Header;
