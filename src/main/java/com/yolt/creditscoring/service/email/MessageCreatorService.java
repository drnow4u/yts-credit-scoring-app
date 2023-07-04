package com.yolt.creditscoring.service.email;

import com.yolt.creditscoring.service.client.ClientEmailDTO;
import com.yolt.creditscoring.service.email.model.InvitationEmailData;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.thymeleaf.ITemplateEngine;
import org.thymeleaf.context.Context;
import software.amazon.awssdk.services.ses.model.Content;
import software.amazon.awssdk.services.ses.model.Message;

import javax.validation.Valid;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class MessageCreatorService {

    private static final String CHARSET = "UTF-8";

    private final ITemplateEngine templateEngine;

    public Message createEmailMessage(@Valid InvitationEmailData creditScoreUserInvitation) {
        ClientEmailDTO clientEmail = creditScoreUserInvitation.getClientEmail();

        Map<String, Object> variablesTemplate = new HashMap<>();
        variablesTemplate.put("userName", creditScoreUserInvitation.getUserName());
        variablesTemplate.put("redirectUrl", creditScoreUserInvitation.getRedirectUrl());
        variablesTemplate.put("clientLogoUrl", creditScoreUserInvitation.getClientLogoUrl());
        variablesTemplate.put("title", clientEmail.getTitle());
        variablesTemplate.put("subtitle", clientEmail.getSubtitle());
        variablesTemplate.put("welcomeBox", clientEmail.getWelcomeBox());
        variablesTemplate.put("buttonText", clientEmail.getButtonText());
        variablesTemplate.put("summaryBox", clientEmail.getSummaryBox());
        variablesTemplate.put("websiteUrl", clientEmail.getWebsiteUrl());

        return Message.builder()
                .subject(s -> s
                        .data(clientEmail.getSubject())
                        .charset(CHARSET))
                .body(b -> b
                        .html(createEmailBodyHtml(variablesTemplate)))
                .build();
    }

    private Content createEmailBodyHtml(Map<String, Object> templateVariables) {
        String output = templateEngine.process("html/UserInvitation_template", new Context(Locale.getDefault(), templateVariables));

        return Content.builder()
                .data(output)
                .charset(CHARSET)
                .build();
    }
}
