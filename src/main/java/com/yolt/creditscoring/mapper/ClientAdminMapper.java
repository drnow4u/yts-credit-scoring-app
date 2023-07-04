package com.yolt.creditscoring.mapper;

import com.yolt.creditscoring.service.client.onboarding.ClientAdminOnboarding;
import com.yolt.creditscoring.service.clientadmin.model.ClientAdmin;
import org.apache.commons.lang3.StringUtils;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;
import java.nio.file.Files;
import java.util.UUID;

@Mapper(componentModel = "spring", imports = {UUID.class})
public abstract class ClientAdminMapper {

    @Mapping(target = "id", expression = "java( UUID.randomUUID() )")
    public abstract ClientAdmin onboardClientAdminToClientAdminEntity(ClientAdminOnboarding clientAdminOnboarding, UUID clientId);

    @Named("content")
    private String resourceAsString(String resourceFilePath) throws IOException {
        byte[] fileContent = Files.readAllBytes(new ClassPathResource(resourceFilePath).getFile().toPath());
        return StringUtils.normalizeSpace(new String(fileContent));
    }
}
