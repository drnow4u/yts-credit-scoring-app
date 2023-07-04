package com.yolt.creditscoring.configuration.validation.constraints;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Validate e-mail address with user's name e.g. John Doe <john.doe@yolt.com>
 * {@link javax.validation.constraints.Email} is only validating e-mail address.
 */
@Target({PARAMETER, FIELD})
@Retention(RUNTIME)
@Constraint(validatedBy = {EmailAddressValidator.class})
@Documented
public @interface EmailAddress {
    String message() default "must be a well-formed e-mail address optionally with user name";

    Class<?>[] groups() default { };

    Class<? extends Payload>[] payload() default { };
}
