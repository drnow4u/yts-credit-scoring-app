package com.yolt.creditscoring.configuration.security.admin;

import org.springframework.security.core.annotation.AuthenticationPrincipal;

import java.lang.annotation.*;

@Target({ElementType.PARAMETER, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@AuthenticationPrincipal(expression = "@adminPrincipalFactory.createClientAdmin(#this)")
public @interface ClientAdminAuthenticationPrincipal {
}
