package com.yolt.creditscoring.service.securitymodule.signature;

import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.PastOrPresent;
import java.time.OffsetDateTime;
import java.util.UUID;

@Data
@Table(name = PublicKeyEntity.TABLE_NAME)
@Entity
public class PublicKeyEntity {

    public static final String TABLE_NAME = "public_key";

    @Id
    private UUID kid;

    @PastOrPresent
    private OffsetDateTime createdDate;

    @NotNull
    private byte[] publicKey;
}
