package com.yolt.creditscoring.controller.logo;

import com.yolt.creditscoring.IntegrationTest;
import com.yolt.creditscoring.service.client.ClientStorageService;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import javax.imageio.ImageIO;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Objects;

import static java.util.UUID.randomUUID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@IntegrationTest
class LogoControllerIT {

    private static final String CLIENT_LOGO_ENDPOINT = "/clients/{clientId}/logo";
    private static final String QUERY_PARAM_MAX_WIDTH = "maxWidth";
    private static final String QUERY_PARAM_MAX_HEIGHT = "maxHeight";

    private static byte[] yoltLogo;

    @MockBean
    private ClientStorageService clientStorageService;

    @Autowired
    private MockMvc mockMvc;

    @BeforeAll
    static void beforeAll() throws IOException {
        yoltLogo = Objects.requireNonNull(LogoControllerIT.class
                        .getClassLoader()
                        .getResourceAsStream("images/yolt.png")) // 1200x630
                .readAllBytes();
    }

    @Test
    void getClientLogo_noMaxWidthHeight_returnsClientLogoAsIs() throws Exception {
        var clientId = randomUUID();

        when(clientStorageService.getClientLogo(clientId)).thenReturn(yoltLogo);

        mockMvc.perform(get(CLIENT_LOGO_ENDPOINT, clientId))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.IMAGE_PNG))
                .andExpect(content().bytes(yoltLogo));
    }

    @Test
    void getClientLogo_maxHeightBiggerThanLogo_returnsClientLogoAsIs() throws Exception {
        var clientId = randomUUID();

        when(clientStorageService.getClientLogo(clientId)).thenReturn(yoltLogo);

        mockMvc.perform(get(CLIENT_LOGO_ENDPOINT, clientId)
                        .param(QUERY_PARAM_MAX_HEIGHT, "631"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.IMAGE_PNG))
                .andExpect(content().bytes(yoltLogo));
    }

    @Test
    void getClientLogo_maxWidthSmallerThanLogo_returnsProperlyScaledClientLogo() throws Exception {
        var clientId = randomUUID();

        when(clientStorageService.getClientLogo(clientId)).thenReturn(yoltLogo);

        var result = mockMvc.perform(get(CLIENT_LOGO_ENDPOINT, clientId)
                        .param(QUERY_PARAM_MAX_WIDTH, "750"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.IMAGE_PNG))
                .andReturn();

        var scaledLogo = ImageIO.read(new ByteArrayInputStream(result.getResponse().getContentAsByteArray()));
        assertThat(scaledLogo.getWidth()).isEqualTo(750);
        assertThat(scaledLogo.getHeight()).isEqualTo(393);
    }

    @Test
    void getClientLogo_maxHeightSmallerThanLogo_returnsProperlyScaledClientLogo() throws Exception {
        var clientId = randomUUID();

        when(clientStorageService.getClientLogo(clientId)).thenReturn(yoltLogo);

        var result = mockMvc.perform(get(CLIENT_LOGO_ENDPOINT, clientId)
                        .param(QUERY_PARAM_MAX_HEIGHT, "500"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.IMAGE_PNG))
                .andReturn();

        var scaledLogo = ImageIO.read(new ByteArrayInputStream(result.getResponse().getContentAsByteArray()));
        assertThat(scaledLogo.getWidth()).isEqualTo(952);
        assertThat(scaledLogo.getHeight()).isEqualTo(500);
    }

    @Test
    void getClientLogo_maxHeightAndWidthSmallerThanLogo_returnsProperlyScaledClientLogo() throws Exception {
        var clientId = randomUUID();

        when(clientStorageService.getClientLogo(clientId)).thenReturn(yoltLogo);

        var result = mockMvc.perform(get(CLIENT_LOGO_ENDPOINT, clientId)
                        .param(QUERY_PARAM_MAX_WIDTH, "175")
                        .param(QUERY_PARAM_MAX_HEIGHT, "55"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.IMAGE_PNG))
                .andReturn();

        var scaledLogo = ImageIO.read(new ByteArrayInputStream(result.getResponse().getContentAsByteArray()));
        assertThat(scaledLogo.getWidth()).isEqualTo(104);
        assertThat(scaledLogo.getHeight()).isEqualTo(55);
    }

    @ParameterizedTest
    @CsvSource({
            "-1,",
            ",-1",
            "-1,55",
            "31,",
            ",31",
            "1025,",
            ",1025",
            "200,1025"
    })
    void getClientLogo_illegalWidthOrHeight_returnsValidationError(Integer optionalMaxWidth, Integer optionalMaxHeight) throws Exception {
        var clientId = randomUUID();

        var request = get(CLIENT_LOGO_ENDPOINT, clientId);
        if (optionalMaxWidth != null) {
            request.queryParam(QUERY_PARAM_MAX_WIDTH, String.valueOf(optionalMaxWidth));
        }
        if (optionalMaxHeight != null) {
            request.queryParam(QUERY_PARAM_MAX_HEIGHT, String.valueOf(optionalMaxHeight));
        }

        when(clientStorageService.getClientLogo(clientId)).thenReturn(yoltLogo);

        mockMvc.perform(request)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("CSA1008")); // Request body validation errors, mapped through LBC
    }
}
