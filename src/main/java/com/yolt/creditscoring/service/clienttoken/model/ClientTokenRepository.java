package com.yolt.creditscoring.service.clienttoken.model;

import org.springframework.data.repository.CrudRepository;

import java.util.List;
import java.util.UUID;

public interface ClientTokenRepository extends CrudRepository<ClientTokenEntity, UUID> {

    long countByClientIdAndStatus(UUID clientId, ClientTokenStatus clientTokenStatus);

    List<ClientTokenEntity> findAllByClientId(UUID clientId);
}
