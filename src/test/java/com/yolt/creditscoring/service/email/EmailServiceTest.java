package com.yolt.creditscoring.service.email;

import com.yolt.creditscoring.exception.EmailSendException;
import com.yolt.creditscoring.service.client.ClientEmailDTO;
import com.yolt.creditscoring.service.email.model.InvitationEmailData;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.services.ses.SesClient;
import software.amazon.awssdk.services.ses.model.Destination;
import software.amazon.awssdk.services.ses.model.Message;
import software.amazon.awssdk.services.ses.model.SendEmailRequest;
import software.amazon.awssdk.services.ses.model.SesException;

import java.util.UUID;

import static com.yolt.creditscoring.TestUtils.SOME_USER_EMAIL;
import static java.util.UUID.randomUUID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EmailServiceTest {

    private static final String SOME_SENDER_EMAIL = "Cashflow Analyser <no-reply-cashflow-analyser@yolt.com>";

    private static final InvitationEmailData SOME_INVITATION_EMAIL_DATA = InvitationEmailData.builder()
            .clientEmail(ClientEmailDTO.builder()
                    .id(randomUUID())
                    .sender(SOME_SENDER_EMAIL)
                    .subject("Hello")
                    .template("SomeTemplate")
                    .build())
            .recipientEmail(SOME_USER_EMAIL)
            .userName("John Doe")
            .clientLogoUrl("https://email.img")
            .redirectUrl("https://redirect.me")
            .build();

    @Mock
    private SesClient sesClient;

    @Mock
    private MessageCreatorService messageCreatorService;

    @Mock
    private EmailStubService emailStubService;

    @InjectMocks
    private EmailService emailService;

    @Test
    void shouldCorrectlySendEmailMessageWhenNotStubClient() {
        final Message emailMessage = Message.builder()
                .body(b -> b
                        .text(t -> t
                                .charset("Sample e-mail message")
                        ))
                .build();

        when(emailStubService.shouldBeStubbed(any())).thenReturn(false);
        when(messageCreatorService.createEmailMessage(SOME_INVITATION_EMAIL_DATA)).thenReturn(emailMessage);

        emailService.sendInvitationForUser(randomUUID(), randomUUID(), SOME_INVITATION_EMAIL_DATA);

        SendEmailRequest expectedEmailObject = SendEmailRequest.builder()
                .source(SOME_SENDER_EMAIL)
                .destination(Destination.builder().toAddresses(SOME_USER_EMAIL).build())
                .message(emailMessage)
                .build();
        verify(sesClient).sendEmail(expectedEmailObject);
    }

    @Test
    void shouldThrowEmailSendExceptionWhenErrorOccurredDuringSendingWhenNotStubClient() {
        // given
        when(emailStubService.shouldBeStubbed(any())).thenReturn(false);
        when(sesClient.sendEmail(any(SendEmailRequest.class)))
                .thenThrow(SesException.builder()
                        .message("SomeException")
                        .build());

        // When
        Throwable thrown = catchThrowable(() -> emailService.sendInvitationForUser(
                randomUUID(), randomUUID(), SOME_INVITATION_EMAIL_DATA
        ));

        // Then
        assertThat(thrown).isInstanceOf(EmailSendException.class)
                .hasMessageContaining("There was an error when sending message");
    }

    @Test
    void shouldCallEmailStubServiceWhenStubClient() {
        UUID clientId = randomUUID();
        UUID userId = randomUUID();

        when(emailStubService.shouldBeStubbed(any())).thenReturn(true);

        emailService.sendInvitationForUser(clientId, userId, SOME_INVITATION_EMAIL_DATA);

        verify(emailStubService).sendInvitationForUser(clientId, userId, SOME_INVITATION_EMAIL_DATA);
    }
}
