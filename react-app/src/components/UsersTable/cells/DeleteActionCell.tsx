import { Button, ChakraIcons } from "@yolt/design-system";

import { useDeleteUser } from "api/admin/user";
import useThrottle from "helpers/useThrottle";

import type { CellProps } from "react-table";

const { DeleteIcon } = ChakraIcons;

function DeleteActionCell<D extends object>({ value }: CellProps<D, string>) {
  const { mutate: deleteUser } = useDeleteUser();
  const throttledDeleteUser = useThrottle(() => deleteUser(value));

  return (
    <Button variant="link" onClick={throttledDeleteUser}>
      <DeleteIcon color="yolt.destructive" />
    </Button>
  );
}

export default DeleteActionCell;
