package com.yolt.creditscoring.service.clientadmin.model;

import org.springframework.data.repository.CrudRepository;

import java.util.Optional;
import java.util.UUID;

public interface ClientAdminRepository extends CrudRepository<ClientAdmin, UUID> {

    Optional<ClientAdmin> findByIdpId(String idpId);


    Optional<ClientAdmin> findByIdpIdAndAuthProvider(String idpId, AuthProvider oAuthProvider);

    void deleteByIdpId(UUID idpId);
}
