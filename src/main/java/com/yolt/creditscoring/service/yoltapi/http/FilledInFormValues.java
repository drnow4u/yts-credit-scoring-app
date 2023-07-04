package com.yolt.creditscoring.service.yoltapi.http;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class FilledInFormValues {
    private final String fieldId;
    private final String value;
}
