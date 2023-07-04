import { useState } from "react";

import { THROTTLE_API_CALL_TIMEOUT } from "./constants";

export function useThrottle(
  func: Function,
  delay: number = THROTTLE_API_CALL_TIMEOUT
) {
  const [timeout, saveTimeout] = useState(null);

  const throttledFunc = function (args: any) {
    if (timeout) {
      clearTimeout(timeout);
    }

    const newTimeout = setTimeout(() => {
      func(args);
      if (newTimeout === timeout) {
        saveTimeout(null);
      }
    }, delay);

    saveTimeout(newTimeout as any);
  };

  return throttledFunc;
}

export default useThrottle;
