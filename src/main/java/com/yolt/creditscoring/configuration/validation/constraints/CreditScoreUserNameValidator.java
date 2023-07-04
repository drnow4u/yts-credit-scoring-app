package com.yolt.creditscoring.configuration.validation.constraints;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CreditScoreUserNameValidator implements ConstraintValidator<CreditScoreUserName, String> {
    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        Pattern pattern = Pattern.compile("^[\\p{L}][\\p{L}\\s'-]{0,254}[\\p{L}]$", Pattern.UNICODE_CHARACTER_CLASS);
        Matcher matcher = pattern.matcher(value);
        try {
            return matcher.matches();
        } catch (Exception e) {
            return false;
        }
    }
}
