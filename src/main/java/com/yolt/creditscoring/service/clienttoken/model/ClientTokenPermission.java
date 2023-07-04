package com.yolt.creditscoring.service.clienttoken.model;

import lombok.Getter;

public enum ClientTokenPermission {
    INVITE_USER(Permissions.INVITE_USER), DOWNLOAD_REPORT(Permissions.DOWNLOAD_REPORT), DELETE_USER(Permissions.DELETE_USER);

    @Getter
    String permissionName;

    ClientTokenPermission(String permissionName) {
        this.permissionName = permissionName;
    }

    /**
     * Constants so they can be used in @Preauthorized expressions. This is also the value that will be set as 'granted authority'.
     */
    public static class Permissions {
        public static final String INVITE_USER = "INVITE_USER";
        public static final String DOWNLOAD_REPORT = "DOWNLOAD_REPORT";
        public static final String DELETE_USER = "DELETE_USER";
    }
}
