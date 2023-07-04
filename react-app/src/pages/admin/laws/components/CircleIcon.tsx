import { ChakraIcons } from "@yolt/design-system";

const { Icon } = ChakraIcons;

export const CircleIcon = ({ color = "yolt.heroBlue" }) => (
  <Icon boxSize={2} viewBox="0 0 200 200" color={color}>
    <path
      fill="currentColor"
      d="M 100, 100 m -75, 0 a 75,75 0 1,0 150,0 a 75,75 0 1,0 -150,0"
    />
  </Icon>
);

export default CircleIcon;
