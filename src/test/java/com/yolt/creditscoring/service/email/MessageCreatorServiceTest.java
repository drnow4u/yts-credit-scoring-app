package com.yolt.creditscoring.service.email;

import com.yolt.creditscoring.service.client.ClientEmailDTO;
import com.yolt.creditscoring.service.email.model.InvitationEmailData;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.thymeleaf.ITemplateEngine;
import software.amazon.awssdk.services.ses.model.Content;
import software.amazon.awssdk.services.ses.model.Message;

import static com.yolt.creditscoring.TestUtils.SOME_USER_NAME;
import static java.util.UUID.randomUUID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MessageCreatorServiceTest {
    private static final String SOME_REDIRECT_URL = "someRedirectUrl.com";
    private static final String SOME_EMAIL_IMAGE_URL = "emailImageUrl.com";

    @Mock
    private ITemplateEngine templateEngine;

    @InjectMocks
    private MessageCreatorService messageCreatorService;

    @Test
    void shouldCorrectlyCreateMessageObject() {
        // given
        String someSenderEmail = "sender@yolt.com";
        String someSubject = "Email subject";
        String someTemplateName = "template_name";

        InvitationEmailData creditScoreUserInvitation = InvitationEmailData.builder()
                .clientEmail(ClientEmailDTO.builder()
                        .id(randomUUID())
                        .sender(someSenderEmail)
                        .subject(someSubject)
                        .template(someTemplateName)
                        .build())
                .recipientEmail("john@doe.com")
                .userName(SOME_USER_NAME)
                .clientLogoUrl(SOME_EMAIL_IMAGE_URL)
                .redirectUrl(SOME_REDIRECT_URL)
                .build();

        when(templateEngine.process(eq("html/UserInvitation_template"), any()))
                .thenReturn("HTML output");

        // when
        Message expected = messageCreatorService.createEmailMessage(creditScoreUserInvitation);

        // then
        assertThat(expected.subject()).isEqualTo(
                Content.builder().data(someSubject).charset("UTF-8").build()
        );
        assertThat(expected.body().text()).isNull();
        assertThat(expected.body().html()).isEqualTo(
                Content.builder().data("HTML output").charset("UTF-8").build()
        );
    }
}
