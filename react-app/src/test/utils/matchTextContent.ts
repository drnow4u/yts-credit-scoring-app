import { MatcherFunction } from "@testing-library/react";

export const matchTextContent =
  (text: string): MatcherFunction =>
  (_, node) => {
    const hasText = (node: Element | null) => node?.textContent === text;
    const nodeHasText = hasText(node);
    const childrenDontHaveText = node
      ? Array.from(node.children).every((child) => !hasText(child))
      : true;

    return nodeHasText && childrenDontHaveText;
  };
