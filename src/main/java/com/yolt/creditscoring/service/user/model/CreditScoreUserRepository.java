package com.yolt.creditscoring.service.user.model;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.CrudRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CreditScoreUserRepository extends CrudRepository<CreditScoreUser, UUID> {

    List<CreditScoreUser> findAllByClientId(UUID clientId);

    Page<CreditScoreUser> findByClientId(UUID clientId, Pageable pageable);

    Optional<CreditScoreUser> findByInvitationHash(String invitationHash);
}
