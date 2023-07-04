import { Chakra } from "@yolt/design-system";

import { OverviewItem, OverviewItemData } from "./OverviewItem";

const { Grid, GridItem } = Chakra;

interface OverviewBoxProps {
  items: OverviewItemData[];
  fit?: boolean;
}

export function OverviewBox({ items, fit = false }: OverviewBoxProps) {
  return (
    <Grid
      bg="white"
      gridGap={8}
      width="100%"
      gridAutoFlow="row dense"
      gridTemplateColumns={{
        base: "repeat(1, 1fr)",
        md: "repeat(2, 1fr)",
        lg: "repeat(3, 1fr)",
        xl: `repeat(${fit ? 3 : 4}, 1fr)`,
        "2xl": `repeat(${fit ? 5 : 6}, 1fr)`,
        "3xl": `repeat(${fit ? 7 : 8}, 1fr)`,
      }}
    >
      {items.map((item) => (
        <GridItem key={item.title}>
          <OverviewItem {...item} />
        </GridItem>
      ))}
    </Grid>
  );
}
