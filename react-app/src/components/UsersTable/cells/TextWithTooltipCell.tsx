import { Chakra } from "@yolt/design-system";

import type { CellProps } from "react-table";

const { Text, Tooltip } = Chakra;

function TextWithTooltipCell<D extends object>({
  value,
}: CellProps<D, string>) {
  return (
    <Tooltip label={value}>
      <Text as="span" isTruncated>
        {value}
      </Text>
    </Tooltip>
  );
}

export default TextWithTooltipCell;
