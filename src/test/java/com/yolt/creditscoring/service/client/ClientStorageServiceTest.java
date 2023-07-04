package com.yolt.creditscoring.service.client;

import com.yolt.creditscoring.exception.ClientNotFoundException;
import com.yolt.creditscoring.service.client.model.ClientEmailEntity;
import com.yolt.creditscoring.service.client.model.ClientEmailRepository;
import com.yolt.creditscoring.service.client.model.ClientEntity;
import com.yolt.creditscoring.service.client.model.ClientRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static com.yolt.creditscoring.TestUtils.SOME_CLIENT_ADDITIONAL_TEXT;
import static com.yolt.creditscoring.TestUtils.SOME_CLIENT_ID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class ClientStorageServiceTest {

    @Mock
    private ClientRepository clientRepository;

    @Mock
    private ClientEmailRepository clientEmailRepository;

    @InjectMocks
    private ClientStorageService clientService;

    @Test
    public void shouldFetchClientAdditionalTextByClientId() {
        // Given
        ClientEntity client = new ClientEntity();
        client.setId(SOME_CLIENT_ID);
        client.setAdditionalTextReport(SOME_CLIENT_ADDITIONAL_TEXT);
        given(clientRepository.findById(SOME_CLIENT_ID)).willReturn(Optional.of(client));

        // When
        String result = clientService.getClientAdditionalReportTextBasedOnClientId(SOME_CLIENT_ID);

        // Then
        assertThat(result).isEqualTo(SOME_CLIENT_ADDITIONAL_TEXT);
    }

    @Test
    public void shouldReturnNullValueIfClientWillNotHaveAdditionalText() {
        // Given
        ClientEntity client = new ClientEntity();
        client.setId(SOME_CLIENT_ID);
        given(clientRepository.findById(SOME_CLIENT_ID)).willReturn(Optional.of(client));

        // When
        String result = clientService.getClientAdditionalReportTextBasedOnClientId(SOME_CLIENT_ID);

        // Then
        assertThat(result).isNull();
    }

    @Test
    public void shouldThrowResourceNotFoundExceptionIfClientWillNotBeFoundWhenFetchingAdditionalText() {
        // Given
        given(clientRepository.findById(SOME_CLIENT_ID)).willReturn(Optional.empty());

        // When
        Throwable thrown = catchThrowable(() -> clientService.getClientAdditionalReportTextBasedOnClientId(SOME_CLIENT_ID));

        // Then
        assertThat(thrown).isInstanceOf(ClientNotFoundException.class);
    }

    @Test
    public void shouldThrowExceptionWhenGetOnlyClientEmailConfigurationByClientIdMethodWouldFetchMoreTheOneTemplate() {
        // Given
        given(clientEmailRepository.findByClient_Id(SOME_CLIENT_ID)).willReturn(
                List.of(
                        new ClientEmailEntity(UUID.randomUUID(), "Template 1", "Subject 1", "Sender 1", null, null, null, null, null, null, null),
                        new ClientEmailEntity(UUID.randomUUID(), "Template 2", "Subject 2", "Sender 2", null, null, null, null, null, null, null)
                )
        );

        // When
        Throwable thrown = catchThrowable(() -> clientService.getOnlyClientEmailByClientId(SOME_CLIENT_ID));

        // Then
        assertThat(thrown).isInstanceOf(IllegalStateException.class);
    }
}
