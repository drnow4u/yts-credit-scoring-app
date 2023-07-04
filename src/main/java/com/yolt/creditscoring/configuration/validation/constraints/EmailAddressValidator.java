package com.yolt.creditscoring.configuration.validation.constraints;

import jakarta.mail.internet.AddressException;
import jakarta.mail.internet.InternetAddress;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class EmailAddressValidator implements ConstraintValidator<EmailAddress, String> {
    @Override
    public boolean isValid(String email, ConstraintValidatorContext context) {
        try {
            InternetAddress ia = new InternetAddress(email);
            ia.validate();
        } catch (AddressException e) {
            return false;
        }
        return true;
    }
}
