package com.yolt.creditscoring.service.clienttoken.model;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import javax.validation.constraints.FutureOrPresent;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.PastOrPresent;
import javax.validation.constraints.Size;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;


@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = ClientTokenEntity.TABLE_NAME)
@Entity
@Builder
public class ClientTokenEntity {

    public static final String TABLE_NAME = "client_token";

    @Id
    @NotNull
    private UUID jwtId;

    @NotNull
    private UUID signedPublicKeyId;

    @NotNull
    private UUID clientId;

    @NotNull
    private String createdAdminEmail;

    @NotNull
    @Size(max = 256)
    private String name;

    @NotNull
    @PastOrPresent
    private OffsetDateTime createdDate;

    @NotNull
    @FutureOrPresent
    private OffsetDateTime expirationDate;

    @PastOrPresent
    private OffsetDateTime lastAccessedDate;

    @NotNull
    @Enumerated(EnumType.STRING)
    private ClientTokenStatus status;

    @NotNull
    @ElementCollection(targetClass = ClientTokenPermission.class, fetch = FetchType.EAGER)
    @CollectionTable(name = "client_token_permission", joinColumns = @JoinColumn(name = "jwt_id"))
    @Column(name = "permission", nullable = false)
    @Enumerated(EnumType.STRING)
    private List<ClientTokenPermission> permissions;
}
