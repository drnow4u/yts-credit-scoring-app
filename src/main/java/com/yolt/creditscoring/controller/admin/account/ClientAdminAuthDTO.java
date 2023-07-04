package com.yolt.creditscoring.controller.admin.account;

import com.yolt.creditscoring.service.client.ClientSettingsDTO;
import lombok.Builder;
import lombok.Value;

import javax.validation.Valid;

@Value
@Builder
public class ClientAdminAuthDTO {

    String email;
    String name;

    @Valid
    ClientSettingsDTO clientSettings;
}
