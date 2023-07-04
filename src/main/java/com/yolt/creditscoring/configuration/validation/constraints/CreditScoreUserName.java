package com.yolt.creditscoring.configuration.validation.constraints;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Target({FIELD})
@Retention(RUNTIME)
@Constraint(validatedBy = {CreditScoreUserNameValidator.class})
@Documented
public @interface CreditScoreUserName {
    String message() default "must be a well-formed user name";

    Class<?>[] groups() default { };

    Class<? extends Payload>[] payload() default { };
}
