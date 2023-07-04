package com.yolt.creditscoring.service.clienttoken;

import com.yolt.creditscoring.service.clienttoken.model.ClientTokenPermission;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.List;

public record CreateClientTokenRequestDTO(@NotNull String name,
                                          @NotEmpty List<ClientTokenPermission> permissions) {

}
