package com.yolt.creditscoring.configuration.security;

import lombok.experimental.UtilityClass;

/**
 * See readme 'Actors on CFA'
 */
@UtilityClass
public class SecurityRoles {

    public final String ROLE_PREFIX = "ROLE_";

    public final String CREDIT_SCORE_USER = "CREDIT_SCORE_USER";
    public final String CLIENT_ADMIN = "CLIENT_ADMIN";
    public final String CLIENT_TOKEN = "CLIENT_TOKEN";
    public final String CFA_ADMIN = "CFA_ADMIN";
}