package com.yolt.creditscoring.controller.user.site;

import lombok.Builder;
import lombok.Value;

import java.util.UUID;

@Builder
@Value
public class SiteViewDTO {
    UUID id;
    String name;
}
