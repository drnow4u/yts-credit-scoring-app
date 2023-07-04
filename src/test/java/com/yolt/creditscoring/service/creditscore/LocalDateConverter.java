package com.yolt.creditscoring.service.creditscore;

import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.params.converter.ArgumentConversionException;
import org.junit.jupiter.params.converter.ArgumentConverter;

import java.time.LocalDate;

public class LocalDateConverter implements ArgumentConverter {

    @Override
    public Object convert(Object source, ParameterContext context) throws ArgumentConversionException {
        if (!(source instanceof String)) {
            throw new IllegalArgumentException(
                    "The argument should be a string: " + source);
        }
        try {
            return "now()".equals(source) ? LocalDate.now() : LocalDate.parse((String) source);
        } catch (Exception e) {
            throw new IllegalArgumentException("Failed to convert", e);
        }
    }
}
