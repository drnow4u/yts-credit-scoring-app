package com.yolt.creditscoring.controller.user.invitation;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class ConsentViewDTO {

    String token;
}
