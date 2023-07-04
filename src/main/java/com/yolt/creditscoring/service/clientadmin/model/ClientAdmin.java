package com.yolt.creditscoring.service.clientadmin.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotNull;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = ClientAdmin.TABLE_NAME)
@Entity
public class ClientAdmin {

    public static final String TABLE_NAME = "client_admin";

    @Id
    private UUID id;

    @Email
    private String email;

    @NotNull
    private UUID clientId;

    @NotNull
    private String idpId;

    @Enumerated(EnumType.STRING)
    private AuthProvider authProvider;
}
