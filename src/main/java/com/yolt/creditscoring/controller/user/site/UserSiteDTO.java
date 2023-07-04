package com.yolt.creditscoring.controller.user.site;

import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
public class UserSiteDTO {
    /*
     * It is not required to validate any correctness of URL or sanitization.
     * Conversation with #yolt-security:
     * https://lovebirdteam.slack.com/archives/CKLQ6C7BN/p1640012652447500
     */
    @NotBlank
    String url;
}
