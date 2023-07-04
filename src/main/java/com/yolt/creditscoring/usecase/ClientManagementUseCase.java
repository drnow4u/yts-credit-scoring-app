package com.yolt.creditscoring.usecase;

import com.yolt.creditscoring.configuration.annotation.UseCase;
import com.yolt.creditscoring.mapper.ClientAdminMapper;
import com.yolt.creditscoring.mapper.ClientMapper;
import com.yolt.creditscoring.service.client.model.ClientEmailEntity;
import com.yolt.creditscoring.service.client.model.ClientEmailRepository;
import com.yolt.creditscoring.service.client.model.ClientEntity;
import com.yolt.creditscoring.service.client.model.ClientRepository;
import com.yolt.creditscoring.service.client.onboarding.ClientAdminOnboarding;
import com.yolt.creditscoring.service.client.onboarding.ClientUpdate;
import com.yolt.creditscoring.service.client.onboarding.OnboardClient;
import com.yolt.creditscoring.service.client.onboarding.OnboardClientEmail;
import com.yolt.creditscoring.service.clientadmin.model.ClientAdmin;
import com.yolt.creditscoring.service.clientadmin.model.ClientAdminRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import javax.validation.Valid;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service is used for client management - it enables client onboarding and updating data on existing client.
 * Method onApplicationEvent is being executed on application startup for given environment.
 * <p>
 * Current production clients:
 * Test Client           - ID: 0b4cee11-0bd6-4e86-806f-45c913ad7bd5

 */
@Slf4j
@UseCase
@Validated
class ClientManagementUseCase {

    private final ClientRepository clientRepository;
    private final ClientEmailRepository clientEmailRepository;
    private final ClientAdminRepository clientAdminRepository;
    private final String environment;
    private final ClientMapper clientMapper;
    private final ClientAdminMapper clientAdminMapper;

    ClientManagementUseCase(ClientRepository clientRepository,
                            ClientEmailRepository clientEmailRepository,
                            ClientAdminRepository clientAdminRepository,
                            @Value("${environment:}") String environment,
                            ClientMapper clientMapper,
                            ClientAdminMapper clientAdminMapper) {
        this.clientRepository = clientRepository;
        this.clientEmailRepository = clientEmailRepository;
        this.clientAdminRepository = clientAdminRepository;
        this.environment = environment;
        this.clientMapper = clientMapper;
        this.clientAdminMapper = clientAdminMapper;
    }

    @EventListener
    public void onApplicationEvent(ContextRefreshedEvent event) throws IOException {
        log.info("Starting client management service");

        if (Set.of("team4", "team5", "team12", "yfb-acc", "yfb-sandbox", "yfb-ext-prd").contains(environment)) {
            removeAllClientAdminsExcept( UUID.fromString("0b4cee11-0bd6-4e86-806f-45c913ad7bd5")
                     );
        }
    }

    @Transactional
    public void onboardNewClient(@Valid OnboardClient onboardClient, @Valid ClientAdminOnboarding... clientAdminUsers) throws IOException {
        Optional<ClientEntity> clientCheck = clientRepository.findById(onboardClient.getId());

        if (clientCheck.isPresent()) {
            log.info("Client with ID: {} - already exists! Skipping adding procedure", onboardClient.getId());
            return;
        }

        ClientEntity client = clientMapper.onboardClientToClientEntity(onboardClient);

        Set<ClientEmailEntity> clientEmailEntities = new HashSet<>();
        for (OnboardClientEmail onboardClientEmail : onboardClient.getClientEmails()) {
            ClientEmailEntity clientEmail = clientMapper.onboardClientEmailToClientEmailEntity(onboardClientEmail, client);
            clientEmailEntities.add(clientEmail);
        }
        if (!clientEmailEntities.isEmpty()) {
            client.setClientEmails(clientEmailEntities);
        }

        List<ClientAdmin> clientAdminList = Arrays.stream(clientAdminUsers)
                .map(clientAdminDTO -> clientAdminMapper.onboardClientAdminToClientAdminEntity(clientAdminDTO, onboardClient.getId()))
                .toList();

        clientRepository.save(client);
        clientAdminRepository.saveAll(clientAdminList);

        log.info("Client with ID: {} - was added", onboardClient.getId());
    }

    public void addNewClientAdminToClient(UUID clientId, ClientAdminOnboarding... clientAdminUsers) {
        List<ClientAdmin> clientAdminList = Arrays.stream(clientAdminUsers)
                .map(clientAdminDTO -> clientAdminMapper.onboardClientAdminToClientAdminEntity(clientAdminDTO, clientId))
                .filter(clientAdmin -> clientAdminRepository.findByIdpId(clientAdmin.getIdpId()).isEmpty())
                .toList();

        if (clientAdminUsers.length == clientAdminList.size()) {
            log.info("All client admin users were inserted for Client with ID: {}", clientId);
        } else {
            log.info("Some admin user where not inserted for Client with ID: {}, as they probably already exist." +
                    "Wanted to add: {}, actually adding: {}", clientId, clientAdminUsers.length, clientAdminList.size());
        }

        clientAdminRepository.saveAll(clientAdminList);
    }

    public void removeAllClientAdminsExcept(UUID... doNotDeleteClientIds) {
        List<UUID> exceptions = Arrays.asList(doNotDeleteClientIds);

        Iterable<ClientAdmin> clientAdmins = clientAdminRepository.findAll();
        for( ClientAdmin clientAdmin : clientAdmins) {
            if ( exceptions.contains(clientAdmin.getClientId() )) {
                log.info("NOT deleting Client admin with id: {},for client: {}", clientAdmin.getId(), clientAdmin.getClientId());
                continue; //skip the exception
            } else {
                clientAdminRepository.delete(clientAdmin);
                log.info("Client admin with id: {}, was removed from client: {}", clientAdmin.getId(), clientAdmin.getClientId());
            }
        }
    }

    public void removeClientAdminFromClient(UUID clientId, String idpId) {
        Optional<ClientAdmin> clientAdmin = clientAdminRepository.findByIdpId(idpId);
        if (clientAdmin.isPresent()) {
            if (clientId.equals(clientAdmin.get().getClientId())) {
                log.info("Client admin with id: {}, was removed from client: {}", clientAdmin.get().getId(), clientId);
                clientAdminRepository.delete(clientAdmin.get());
            }
        }
    }

    public void updateClientData(ClientUpdate clientUpdate) throws IOException {
        Optional<ClientEntity> clientUpdateCheck = clientRepository.findById(clientUpdate.getId());
        Optional<ClientEmailEntity> clientEmailUpdateCheck = Optional.empty();

        if (clientUpdateCheck.isEmpty()) {
            log.info("Client with ID: {} - does not exist! Aborting update.", clientUpdate.getId());
            return;
        }

        log.info("Updating client with ID: {}", clientUpdate.getId());

        if (clientUpdate.getClientEmailUpdate() != null) {
            clientEmailUpdateCheck = clientEmailRepository.findByClient_Id(clientUpdate.getId())
                    .stream()
                    .filter(clientEmailEntity -> clientEmailEntity.getTemplate().equals(clientUpdate.getTemplate()))
                    .findFirst();
        }

        ClientEntity clientForUpdate = clientUpdateCheck.get();

        clientMapper.updateClientEntityFromClientUpdate(clientUpdate, clientForUpdate);

        clientRepository.save(clientForUpdate);

        clientEmailUpdateCheck.ifPresentOrElse(clientEmailForUpdate -> {
            // temporary solution until YTRN-1291 is implemented
            clientMapper.updateClientEmailEntityFromClientEmailUpdate(clientUpdate.getClientEmailUpdate(), clientEmailForUpdate);
            clientEmailRepository.save(clientEmailForUpdate);
        }, () -> log.warn("Template {} not found", clientUpdate.getTemplate()));
    }

    public void addClientEmailConfiguration(UUID clientId, @Valid OnboardClientEmail... clientEmails) {
        Optional<ClientEntity> clientEntity = clientRepository.findById(clientId);

        if (clientEntity.isEmpty()) {
            log.info("Client with ID: {} - does not exist! Aborting update.", clientId);
            return;
        }

        Set<ClientEmailEntity> currentClientEmails = clientEntity.get().getClientEmails();

        Set<ClientEmailEntity> newClientEmails = Arrays.stream(clientEmails)
                .map(onboardClientEmail -> clientMapper.onboardClientEmailToClientEmailEntity(onboardClientEmail, clientEntity.get()))
                .filter(clientEmail -> clientEmailRepository.findByTemplateAndSubjectAndSender(
                        clientEmail.getTemplate(), clientEmail.getSubject(), clientEmail.getSender()).isEmpty()
                )
                .collect(Collectors.toSet());

        if (!newClientEmails.isEmpty()) {
            log.info("Adding {} new email configurations to client ID - {}", newClientEmails.size(), clientId);
            currentClientEmails.addAll(newClientEmails);
            clientRepository.save(clientEntity.get());
        } else {
            log.info("There were no additional email configuration for client ID - {}", clientId);
        }
    }

    public void updateClientEmailConfiguration(UUID clientId, @Valid OnboardClientEmail oldClientEmail, @Valid OnboardClientEmail newClientEmail) {
        Optional<ClientEntity> clientEntity = clientRepository.findById(clientId);

        if (clientEntity.isEmpty()) {
            log.info("Client with ID: {} - does not exist! Aborting update.", clientId);
            return;
        }

        Optional<ClientEmailEntity> currentClientEmail = clientEntity.get().getClientEmails().stream()
                .filter(clientEmailEntity ->
                        Objects.equals(clientEmailEntity.getTemplate(), oldClientEmail.getTemplate()) &&
                                Objects.equals(clientEmailEntity.getSubject(), oldClientEmail.getSubject()) &&
                                Objects.equals(clientEmailEntity.getSender(), oldClientEmail.getSender()))
                .findFirst();


        if (currentClientEmail.isPresent()) {
            log.info("Update old email to new email configurations for client ID - {}", clientId);
            ClientEmailEntity updatedClientEmail = currentClientEmail.get();
            updatedClientEmail.setSender(newClientEmail.getSender());
            updatedClientEmail.setSubject(newClientEmail.getSubject());
            updatedClientEmail.setTemplate(newClientEmail.getTemplate());
            clientEmailRepository.save(updatedClientEmail);
            clientRepository.save(clientEntity.get());
        } else {
            log.info("There were no additional email configuration for client ID - {}", clientId);
        }
    }
}
