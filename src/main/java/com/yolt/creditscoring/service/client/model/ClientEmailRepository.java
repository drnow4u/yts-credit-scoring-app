package com.yolt.creditscoring.service.client.model;


import org.springframework.data.repository.CrudRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ClientEmailRepository  extends CrudRepository<ClientEmailEntity, UUID> {

    List<ClientEmailEntity> findByClient_Id(UUID clientId);

    Optional<ClientEmailEntity> findByTemplateAndSubjectAndSender(String template, String subject, String sender);
}
