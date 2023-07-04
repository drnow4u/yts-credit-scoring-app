package com.yolt.creditscoring.controller.admin.users;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.yolt.creditscoring.service.user.model.InvitationStatus;
import lombok.Builder;
import lombok.Value;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotNull;
import java.time.OffsetDateTime;
import java.util.UUID;

@Value
@Builder
public class ViewUserDTO {

    UUID userId;

    String email;

    String name;

    @Email
    @NotNull
    String adminEmail;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'", timezone = "UTC")
    OffsetDateTime dateInvited;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'", timezone = "UTC")
    OffsetDateTime dateStatusUpdated;

    InvitationStatus status;
}
