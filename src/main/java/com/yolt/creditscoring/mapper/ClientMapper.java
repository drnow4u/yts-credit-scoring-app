package com.yolt.creditscoring.mapper;

import com.yolt.creditscoring.service.client.model.ClientEmailEntity;
import com.yolt.creditscoring.service.client.model.ClientEntity;
import com.yolt.creditscoring.service.client.onboarding.ClientEmailUpdate;
import com.yolt.creditscoring.service.client.onboarding.ClientUpdate;
import com.yolt.creditscoring.service.client.onboarding.OnboardClient;
import com.yolt.creditscoring.service.client.onboarding.OnboardClientEmail;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

import java.io.IOException;
import java.util.UUID;

@Mapper(componentModel = "spring", imports = {UUID.class})
public abstract class ClientMapper {

    @Mapping(target = "clientEmails", ignore = true)
    public abstract ClientEntity onboardClientToClientEntity(OnboardClient onboardClient) throws IOException;

    @Mapping(target = "id", expression = "java( UUID.randomUUID() )")
    @Mapping(target = "client", source = "clientEntity")
    public abstract ClientEmailEntity onboardClientEmailToClientEmailEntity(OnboardClientEmail onboardClientEmail, ClientEntity clientEntity);

    @Mapping(target = "name", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "logo", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "additionalTextConsent", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "additionalTextReport", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "siteTags", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "defaultLanguage", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "PDScoreFeatureToggle", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "signatureVerificationFeatureToggle", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "categoryFeatureToggle", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "monthsFeatureToggle", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "overviewFeatureToggle", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "apiTokenFeatureToggle", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "redirectUrl", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    public abstract void updateClientEntityFromClientUpdate(ClientUpdate clientUpdate, @MappingTarget ClientEntity ClientEntity);

    @Mapping(target = "title", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "subtitle", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "welcomeBox", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "buttonText", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "summaryBox", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    public abstract void updateClientEmailEntityFromClientEmailUpdate(ClientEmailUpdate clientEmailUpdate, @MappingTarget ClientEmailEntity clientEmailEntity);
}
