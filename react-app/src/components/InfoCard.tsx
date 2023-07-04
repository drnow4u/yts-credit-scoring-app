import { Chakra } from "@yolt/design-system";

const { Box, Center, Stack, Text } = Chakra;

export interface InfoCardProps {
  title?: string;
  info: string;
  titleIcon?: JSX.Element;
  selected?: boolean;
}

//TODO: remade for design-system as multi-part themed component
export function InfoCard({ title, info, titleIcon, selected }: InfoCardProps) {
  const mainStyle = selected
    ? {
        backgroundColor: "hsl(216, 94%, 46%)",
        _after: {
          content: '""',
          position: "absolute",
          right: "0",
          top: "50%",
          margin: "0",
          width: "0",
          height: "0",
          borderLeft: "8px solid hsl(216, 94%, 46%)",
          borderTop: "8px solid transparent",
          borderBottom: "8px solid transparent",
          transform: "translate(100%, -50%)",
        },
      }
    : {
        background: "none",
      };
  const iconStyle = selected
    ? {
        path: {
          fill: "hsl(216, 94%, 46%)",
        },
      }
    : {
        path: {
          fill: "hsl(216, 22%, 50%)",
        },
      };
  const textStyle = selected
    ? {
        color: "white",
      }
    : {
        color: "black",
      };
  return (
    <Stack
      position="relative"
      borderWidth="0.1rem"
      borderColor="hsl(216, 22%, 50%)"
      padding="0.5rem"
      direction="row"
      spacing="4"
      sx={mainStyle}
    >
      {titleIcon && (
        <Center padding="0.2rem">
          <Box
            backgroundColor="yolt.backgroundGrey"
            borderRadius="50%"
            width="32px"
            height="32px"
            padding="4px"
            sx={iconStyle}
          >
            {titleIcon}
          </Box>
        </Center>
      )}
      <Stack spacing="2">
        {title && (
          <Text
            isTruncated
            maxWidth="10vw"
            fontSize="xs"
            fontWeight="200"
            sx={textStyle}
          >
            {title}
          </Text>
        )}
        <Text isTruncated maxWidth="10vw" fontWeight="600" sx={textStyle}>
          {info}
        </Text>
      </Stack>
    </Stack>
  );
}
