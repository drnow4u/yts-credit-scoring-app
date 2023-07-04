package com.yolt.creditscoring.service.yoltapi.http;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.List;

@Getter
@RequiredArgsConstructor
public class CreateUserSiteForm {
    private final String stateId;
    private final String loginType = "FORM";
    private final List<FilledInFormValues> filledInFormValues;
}
