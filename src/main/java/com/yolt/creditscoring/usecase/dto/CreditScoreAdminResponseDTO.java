package com.yolt.creditscoring.usecase.dto;

import com.yolt.creditscoring.controller.admin.users.Based64;
import com.yolt.creditscoring.service.creditscore.storage.dto.response.admin.BankAccountDetailsDTO;
import lombok.Builder;
import lombok.NonNull;
import lombok.Value;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotNull;
import java.security.PublicKey;
import java.util.List;

@Value
@Builder(toBuilder = true)
public class CreditScoreAdminResponseDTO {

    @Email
    String userEmail;

    @NotNull
    BankAccountDetailsDTO adminReport;

    @NotNull
    PublicKey publicKey;

    @NotNull
    Based64 signature;

    @NonNull
    List<String> signatureJsonPaths;

    boolean shouldVerifiedSignature;
}
