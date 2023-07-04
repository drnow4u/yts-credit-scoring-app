import { Chakra } from "@yolt/design-system";

const { Stack, Text, Circle } = Chakra;

export interface SummaryCardProps {
  title: string;
  info?: string;
  titleIcon?: JSX.Element;
  titleIconBackground: string;
  items: SummaryCardItemData[];
}

export interface SummaryCardItemData {
  title: string | number;
  info: string;
}

//TODO: remade for design-system as multi-part themed component
export function SummaryCard({
  title,
  info,
  titleIcon,
  titleIconBackground,
  items,
}: SummaryCardProps) {
  return (
    <Stack
      borderWidth="0.1rem"
      borderColor="white"
      padding="0.5rem"
      position="relative"
      spacing="4"
      bg="white"
    >
      <Stack direction="row">
        {titleIcon && (
          <Circle size={10} bg={titleIconBackground} color="white">
            {titleIcon}
          </Circle>
        )}
        <Stack>
          <Text margin={0} maxWidth="10vw" fontWeight="600">
            {title}
          </Text>
          {info && (
            <Text margin={0} fontSize="xs" fontWeight="200">
              {info}
            </Text>
          )}
        </Stack>
      </Stack>
      {items.map((item, index) => (
        <Stack position="relative" key={index}>
          <Text margin={0} maxWidth="10vw" fontWeight="600">
            {item.title}
          </Text>
          {item.info && (
            <Text margin={0} fontSize="sm" fontWeight="200">
              {item.info}
            </Text>
          )}
        </Stack>
      ))}
    </Stack>
  );
}
