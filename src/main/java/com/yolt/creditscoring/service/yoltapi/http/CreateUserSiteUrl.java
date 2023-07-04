package com.yolt.creditscoring.service.yoltapi.http;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

// I didn't found this DTO in OpenAPI generated from Yolt's Swagger
@Getter
@RequiredArgsConstructor
public class CreateUserSiteUrl {
    private final String redirectUrl;
    private final String loginType = "URL";
}
