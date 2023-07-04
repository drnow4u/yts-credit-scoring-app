package com.yolt.creditscoring.configuration.security.admin;

public class AdminClaims {
    // for every admin:
    public static final String ROLES = "roles";
    public static final String EMAIL = "email";
    public static final String IPDID = "idpId";

    // for admins that are also client admin
    public static final String CLIENT_ID = "clientId";
    public static final String ADMIN_ID = "adminId";

}
