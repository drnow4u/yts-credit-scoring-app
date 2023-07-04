package com.yolt.creditscoring.service.clienttoken.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;
import lombok.NonNull;
import lombok.Value;

import javax.validation.constraints.Size;
import java.time.OffsetDateTime;
import java.util.UUID;

@Value
@Builder
public class ClientTokensDTO {

    @NonNull
    UUID id;

    @NonNull
    @Size(max = 256)
    String name;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'", timezone = "UTC")
    OffsetDateTime creationDate;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'", timezone = "UTC")
    OffsetDateTime expiryDate;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'", timezone = "UTC")
    OffsetDateTime lastUsed;

    @NonNull
    ClientTokenStatus status;
}
