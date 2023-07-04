import { FC } from "react";

import { Chakra, useCountDown } from "@yolt/design-system";

type CounterDownProps = {
  countDownMsc: number;
};

const { Text } = Chakra;

export const CounterDown: FC<CounterDownProps> = ({
  countDownMsc,
}: CounterDownProps) => {
  const counterDown = useCountDown(countDownMsc);

  if (counterDown < 0) return null;

  return <Text>{counterDown}</Text>;
};

export default CounterDown;
