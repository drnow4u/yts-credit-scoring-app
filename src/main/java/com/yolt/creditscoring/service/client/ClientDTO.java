package com.yolt.creditscoring.service.client;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class ClientDTO {

    String name;
    String logo;
    String language;
    String additionalTextConsent;
}
