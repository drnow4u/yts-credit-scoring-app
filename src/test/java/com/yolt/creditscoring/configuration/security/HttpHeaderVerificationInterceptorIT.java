package com.yolt.creditscoring.configuration.security;

import com.yolt.creditscoring.IntegrationTest;
import com.yolt.creditscoring.configuration.security.admin.TestUtils;
import com.yolt.creditscoring.service.securitymodule.jwt.JwtCreationService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.test.web.servlet.MockMvc;

import static com.yolt.creditscoring.controller.admin.account.ClientAdminController.ACCOUNT_ENDPOINT;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@IntegrationTest
class HttpHeaderVerificationInterceptorIT {

    private static final String BIG_HTTP_HEADER_VALUE = "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Nunc vitae diam sit amet massa semper consequat. Suspendisse sed fringilla urna. Duis nulla nunc, ullamcorper at orci a, cursus feugiat elit. Maecenas nec purus quis neque semper iaculis at at enim. Sed a luctus enim. Nulla rutrum hendrerit dolor, at porttitor odio imperdiet volutpat. Duis luctus rhoncus suscipit. Pellentesque dignissim ultrices est, vel porta orci molestie vel. Morbi ac leo id erat tempor aliquet. Vestibulum et ligula in ex euismod pharetra. Donec in luctus turpis, a posuere arcu. Cras rhoncus erat in ante laoreet, et lobortis turpis placerat. Phasellus semper ex convallis, imperdiet ex quis, gravida risus. Aliquam elit eros, scelerisque gravida eleifend ut, semper vitae ante. Donec eu augue vestibulum, condimentum purus a, ultricies purus. Suspendisse sollicitudin orci risus, eu interdum augue varius at. Sed consequat vel risus id dignissim. Suspendisse placerat ipsum eget lectus sodales tincidunt. Morbi vel massa sit amet libero convallis consequat at at lorem. Suspendisse sollicitudin eget ligula vel tristique. Sed hendrerit a nisl a laoreet. Maecenas congue, leo id volutpat rhoncus, elit tortor iaculis orci, vel posuere leo tellus nec ex. Fusce pharetra metus et diam facilisis commodo. Vivamus rutrum tortor vel urna auctor tempor. In hac habitasse platea dictumst. Fusce sed neque in purus ullamcorper suscipit eu vel dolor. Nam nec dui eros. Etiam a augue felis. Pellentesque viverra commodo nibh et vulputate. Nullam vitae orci nunc. Ut lacinia lorem libero. Curabitur non dignissim sapien. Cras eget volutpat ex. Fusce aliquet tortor eu tempor porta. Duis malesuada nisl sit amet lorem aliquet auctor. Quisque nec velit neque.Morbi mollis lacus eu massa cursus, sed faucibus quam ornare. Vestibulum maximus eget nunc in pretium. Maecenas non est vel est scelerisque semper. Vivamus quis sem at nulla elementum imperdiet. In sed facilisis odio. Sed dictum et leo non suscipit. Nullam pharetra fringilla metus et consectetur. Sed euismod diam a ipsum tristique, in ultrices mauris luctusDonec eget tellus nec nisi pellentesque auctorEtiam mattis magna ut turpis egestas convallis. Maecenas gravida congue arcu, at auctor neque imperdiet eu. Sed id suscipit enim. Nulla ornare, eros sed rhoncus sagittis, tellus turpis vestibulum dui, sit amet elementum lacus lacus at elit. Maecenas vehicula nunc nibh, sed aliquet nunc commodo id. Nulla consequat augue ac semper sagittis. Ut nec dui id sapien finibus pellentesque. Donec elementum leo velmetus tempus finibus. Maecenas sit amet nunc rhoncus, venenatis dolor a, porttitor enim. Sed non tortor urna. Fusce quis nisl eget diam molestie placerat. Aliquam malesuada est ut sem lacinia, eget tristique urna tincidunt.Nam vel tincidunt nunc. Aenean porta, risus ac bibendum vulputate, tortor metus egestas arcu, in vulputate sem turpis sed nulla. Nunc pulvinar finibus egestas. Interdum et malesuada fames ac ante ipsum primis in faucibus. Pellentesque a pharetra sem, non tempus nisl. Aenean finibus arcu turpis, in finibus enim vehicula fringilla. Praesent ac justo ut nunc pellentesque posuere eget at elit. Morbi nisi elit, fringilla suscipit sem nec, convallis venenatis sapien. Nunc laoreet dapibus odio sit amet consectetur. Fusce rhoncus ipsum magna, eget malesuada justo aliquam pellentesque. Integer dignissim diam leo, ut volutpat felis convallis in. Nullam mollis dolor nibh, ut euismod justo placerat quis. Curabitur ac neque sit amet nunc tincidunt suscipit. Sed congue, massa sit amet eleifend imperdiet, eros magna condimentum leo, vitae bibendum arcu est nec lorem. Cras aliquam pulvinar iaculis. Aenean eget molestie purus, non blandit diam.Nulla nisl sem, semper consectetur elit a, finibus facilisis erat. Integer molestie turpis efficitur tristique mollis. Suspendisse in mi ac odio lacinia pharetra eget a sapien. Etiam vitae venenatis nibh. Integer commodo justo felis, ac venenatis eros fermentum at. Suspendisse molestie nisl eu nisl vehicula, eget scelerisque leo mattis. Aliquam erat volutpat. Quisque at eleifend magna. Ut feugiat, orci ultricies egestas volutpat, nulla ligula porttitor eros, id tincidunt dui tortor at odio.";

    @Autowired
    private MockMvc mvc;

    @Autowired
    private JwtCreationService jwtCreationService;

    @Test
    void shouldThrowExceptionWhenHeaderValueWillBeToBig() throws Exception {
        // When
        mvc.perform(get(ACCOUNT_ENDPOINT)
                        .header(HttpHeaders.AUTHORIZATION, jwtCreationService.createAdminToken(TestUtils.OAUTH_ADMIN_USER_CLIENT_ADMIN))
                        .header("Some-big-header", BIG_HTTP_HEADER_VALUE))
                .andExpect(status().is5xxServerError());
    }

    @Test
    void shouldThrowExceptionWhenHeaderCountWillExtend() throws Exception {
        // When
        mvc.perform(get(ACCOUNT_ENDPOINT)
                        .header(HttpHeaders.AUTHORIZATION, jwtCreationService.createAdminToken(TestUtils.OAUTH_ADMIN_USER_CLIENT_ADMIN))
                        .header("Some-header-2", "Test")
                        .header("Some-header-3", "Test")
                        .header("Some-header-4", "Test")
                        .header("Some-header-5", "Test")
                        .header("Some-header-6", "Test")
                        .header("Some-header-7", "Test")
                        .header("Some-header-8", "Test")
                        .header("Some-header-9", "Test")
                        .header("Some-header-10", "Test")
                        .header("Some-header-11", "Test")
                        .header("Some-header-12", "Test")
                        .header("Some-header-13", "Test")
                        .header("Some-header-14", "Test")
                        .header("Some-header-15", "Test")
                        .header("Some-header-16", "Test")
                        .header("Some-header-17", "Test")
                        .header("Some-header-18", "Test")
                        .header("Some-header-19", "Test")
                        .header("Some-header-20", "Test")
                        .header("Some-header-21", "Test")
                        .header("Some-header-22", "Test")
                        .header("Some-header-23", "Test")
                        .header("Some-header-24", "Test")
                        .header("Some-header-25", "Test")
                        .header("Some-header-26", "Test")
                        .header("Some-header-27", "Test")
                        .header("Some-header-28", "Test")
                        .header("Some-header-29", "Test")
                        .header("Some-header-30", "Test")
                        .header("Some-header-31", "Test")
                        .header("Some-header-32", "Test")
                        .header("Some-header-33", "Test"))
                .andExpect(status().is5xxServerError());
    }
}
