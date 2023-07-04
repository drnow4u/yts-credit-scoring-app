package com.yolt.creditscoring.controller.admin.users;

import com.yolt.creditscoring.configuration.validation.constraints.CreditScoreUserName;
import lombok.Value;

import javax.validation.constraints.Email;
import java.util.UUID;

@Value
public class InviteUserDTO {

    @CreditScoreUserName
    String name;

    @Email
    String email;

    UUID clientEmailId;
}
