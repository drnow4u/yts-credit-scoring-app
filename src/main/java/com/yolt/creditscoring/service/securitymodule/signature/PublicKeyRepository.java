package com.yolt.creditscoring.service.securitymodule.signature;

import org.springframework.data.repository.CrudRepository;

import java.util.UUID;

public interface PublicKeyRepository extends CrudRepository<PublicKeyEntity, UUID> {
}
