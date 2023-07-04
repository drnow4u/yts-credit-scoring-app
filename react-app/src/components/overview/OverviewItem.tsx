import { Chakra } from "@yolt/design-system";

const { Center, Stack, Text, Tooltip } = Chakra;

export interface OverviewItemData {
  title: string;
  info: string;
  titleIcon?: JSX.Element;
  tooltip?: string;
}

export function OverviewItem({
  title,
  info,
  titleIcon,
  tooltip,
}: OverviewItemData) {
  return (
    <Stack>
      <Stack direction="row" spacing={4}>
        {titleIcon && (
          <Center
            color="hsl(216, 94%, 46%)"
            sx={{
              path: {
                fill: "hsl(216, 94%, 46%)",
              },
            }}
          >
            {titleIcon}
          </Center>
        )}
        <Center>
          <Text textColor="yolt.lightGrey" fontWeight="100">
            {title}
          </Text>
        </Center>
      </Stack>
      <Tooltip isDisabled={!tooltip} label={tooltip}>
        <Text fontWeight="100">{info}</Text>
      </Tooltip>
    </Stack>
  );
}
