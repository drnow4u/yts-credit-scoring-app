package com.yolt.creditscoring.service.email;

import com.yolt.creditscoring.exception.EmailSendException;
import com.yolt.creditscoring.service.email.model.InvitationEmailData;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;
import software.amazon.awssdk.services.ses.SesClient;
import software.amazon.awssdk.services.ses.model.Destination;
import software.amazon.awssdk.services.ses.model.Message;
import software.amazon.awssdk.services.ses.model.SendEmailRequest;

import javax.validation.Valid;
import java.util.UUID;

@Service
@Validated
@RequiredArgsConstructor
public class EmailService {

    private final SesClient sesClient;
    private final EmailStubService emailStubService;
    private final MessageCreatorService messageCreatorService;

    public void sendInvitationForUser(UUID clientId, UUID userId, @Valid InvitationEmailData invitationEmailData) {
        if (emailStubService.shouldBeStubbed(clientId)) {
            emailStubService.sendInvitationForUser(clientId, userId, invitationEmailData);
            return;
        }

        sendInvitationForUser(invitationEmailData);
    }

    private void sendInvitationForUser(InvitationEmailData invitationEmailData) {
        Message emailMessage = messageCreatorService.createEmailMessage(invitationEmailData);

        try {
            sesClient.sendEmail(SendEmailRequest.builder()
                    .message(emailMessage)
                    .source(invitationEmailData.getClientEmail().getSender())
                    .destination(Destination.builder().toAddresses(invitationEmailData.getRecipientEmail()).build())
                    .build());
        } catch (Exception e) {
            throw new EmailSendException("There was an error when sending message", e);
        }
    }

}
