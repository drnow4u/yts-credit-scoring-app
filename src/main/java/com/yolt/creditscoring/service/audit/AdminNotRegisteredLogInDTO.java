package com.yolt.creditscoring.service.audit;

import lombok.Builder;
import lombok.Value;

@Builder
@Value
public class AdminNotRegisteredLogInDTO {

    String idpId;
    String provider;
}
