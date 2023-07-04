package com.yolt.creditscoring.service.securitymodule.jwt;

import java.util.UUID;

public record JwtClientToken(UUID jwtId, String encryptedJwt, UUID publicKeyIdForVerification) { }
