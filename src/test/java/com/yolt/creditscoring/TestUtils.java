package com.yolt.creditscoring;

import com.yolt.creditscoring.controller.admin.users.Based64;
import com.yolt.creditscoring.service.legaldocument.model.DocumentType;
import com.yolt.creditscoring.service.legaldocument.model.LegalDocument;
import lombok.experimental.UtilityClass;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.UUID;

import static java.util.UUID.randomUUID;

@UtilityClass
public class TestUtils {

    public static final UUID SOME_USER_ID = UUID.fromString("ba459bdb-5032-43ff-a339-500e9b20cf26");
    public static final UUID SOME_USER_ID_2 = UUID.fromString("ddbe8f45-3b02-4dd6-b258-c607c62446f3");
    public static final UUID SOME_USER_ID_3 = UUID.fromString("81c82164-5f14-456c-b3f9-d9c49df0d1bc");
    public static final UUID SOME_CLIENT_ID = UUID.fromString("0b4cee11-0bd6-4e86-806f-45c913ad7bd5");
    public static final UUID SOME_CLIENT_ID_2 = UUID.fromString("20a29985-8324-4c3d-b6d3-174a10f2bbe2");
    public static final UUID SOME_CLIENT_REQUEST_TOKEN_PUBLIC_KEY_ID = UUID.fromString("80d958a6-516a-4058-a506-e7c7b8f6b255");
    public static final UUID SOME_CLIENT_REDIRECT_URL_ID = UUID.fromString("d40823af-a34f-46e5-9db9-192e9919e400");
    public static final UUID SOME_CLIENT_EMAIL_ID = UUID.fromString("0f3602a8-7e22-4f14-b339-e3c52badc163");
    public static final UUID SOME_CLIENT_JWT_ID = UUID.fromString("7b5ea0fd-b0b2-4985-a609-d056d82b9f08");
    public static final String SOME_CLIENT_JWT_NAME = "JWT";
    public static final String SOME_CLIENT_ENCRYPTED_JWT = "ENCRYPTED_JWT";
    public static final String SOME_CLIENT_REDIRECT_URL = "https://client-redirect.com";
    public static final UUID SOME_YOLT_CLIENT_ID = UUID.fromString("0a3bbcd4-77c1-4a3e-b034-ba391b37d3f8");
    public static final UUID SOME_YOLT_USER_ID = UUID.fromString("497f6eca-6276-4993-bfeb-53cbbbba6f08");
    public static final UUID SOME_YOLT_SITE_ID = UUID.fromString("0b6b75f4-608b-44bb-a03a-2cd340c65d37");
    public static final String SOME_YOLT_REDIRECT_BACK_URL = "http://localhost:3000/yts-credit-scoring-app/site-connect-callback?code=eyJhbGciOiJub25lIn0.eyJleHAiOjE2MTU3MTc1MTYsInN1YiI6IkxJR0hUIiwiaWF0IjoxNjA3OTQxNTE2fQ.&state=d94f2229-dc5a-4a16-97c2-ff35af2a8b8c";
    public static final String SOME_YOLT_REDIRECT_BACK_URL_ERROR = "http://localhost:3000/yts-credit-scoring-app/site-connect-callback?error=access_denied&state=65d1d5ef-733f-4e55-84be-b8420225b4a2";
    public static final String SOME_YOLT_REDIRECT_BACK_URL_MULTI_STEP = "http://localhost:3000/yts-credit-scoring-app/site-connect-callback?code=eyJhbGciOiJub25lIn0.eyJleHAiOjE2MjM2Njc1MzUsInN1YiI6IkxJR0hUIiwiaWF0IjoxNjE1ODkxNTM1fQ.&state=584624b6-7c5b-4174-b9c0-cdd8ef148c99&consent-redirect=yes";
    public static final UUID SOME_YOLT_USER_SITE_ID = UUID.fromString("061ae378-f773-4625-9796-06e72e1e5a86");
    public static final UUID SOME_YOLT_USER_ACTIVITY_ID = UUID.fromString("ffa198a7-dc92-4aad-9237-95c0a045091c");
    public static final UUID SOME_YOLT_USER_ACCOUNT_ID = UUID.fromString("e74fc2f3-847f-4004-93f5-1248d4655ed8");
    public static final OffsetDateTime SOME_TEST_DATE = OffsetDateTime.now();
    public static final OffsetDateTime SOME_FIXED_TEST_DATE = OffsetDateTime.of(2020, 11, 1, 10, 0, 0, 0, ZoneOffset.UTC);
    public static final OffsetDateTime SOME_FIXED_TEST_DATE_2 = OffsetDateTime.of(2020, 11, 1, 11, 0, 0, 0, ZoneOffset.UTC);
    public static final String SOME_USER_NAME = "User Name";
    public static final String SOME_USER_NAME_2 = "User D'Second";
    public static final String SOME_USER_EMAIL = "user@email.com";
    public static final String SOME_USER_EMAIL_2 = "user2@email.com";
    public static final String SOME_USER_AGENT = "Chrome/51.0.2704.103";
    public static final String SOME_USER_IP = "127.0.0.1";
    public static final String SOME_USER_HASH = "a7a40423-c81c-4d4e-9dae-70c4de11d364";
    public static final String SOME_USER_HASH_2 = "c7620ce9-bef8-43b1-96f7-0fcac20a531f";
    public static final String SOME_USER_HASH_3 = "fb7bf551-60f1-42fc-9d0c-48026245fba3";
    public static final String SOME_USER_JWT = "JWT";
    public static final Based64 SOME_REPORT_SIGNATURE = Based64.fromEncoded("1234567890");
    public static final UUID SOME_REPORT_SIGNATURE_KEY_ID = UUID.fromString("0a07c523-86a9-4bf9-9e0f-976beb37bcea");
    public static final UUID SOME_JWT_PUBLIC_KEY_ID = UUID.fromString("9f3ac889-f139-48c8-b5ca-3370f3b7957e");
    public static final String SOME_CLIENT_NAME = "Some Client";
    public static final String SOME_CLIENT_2_NAME = "Some Client 2";
    public static final String SOME_CLIENT_ADMIN_IDP_ID = "f242d4aa-4f4d-49eb-ad92-10eb2f4c65d9";
    public static final UUID SOME_CLIENT_ADMIN_ID = UUID.fromString("c94b8a73-258f-4896-951f-79f981df592c");
    public static final String SOME_CLIENT_ADMIN_EMAIL = "adminuser@test.com";
    public static final String SOME_CLIENT_ADMIN_IP = "127.0.0.1";
    public static final String SOME_CLIENT_ADMIN_USER_AGENT = "Chrome";
    public static final String SOME_CLIENT_ADMIN_2_EMAIL = "admin2@example.com";

    public static final UUID SOME_CLIENT_2_ADMIN_ID = UUID.fromString("62b62834-6b31-4022-97d3-89c6522a13e7");
    public static final String SOME_CLIENT_2_ADMIN_EMAIL = "admin@example2.com";
    public static final String SOME_CLIENT_2_ADMIN_IDP_ID = "2c89590a-20c3-42d2-8b24-0e4cf5ee7499";

    public static final String SOME_CLIENT_ADDITIONAL_TEXT = "Some additional text for test client";
    public static final String SOME_CLIENT_SITE_TAGS = "NL";
    public static final String SOME_CLIENT_LANGUAGE = "nl";
    public static final OffsetDateTime SOME_STILL_VALID_INVITATION_DATE = OffsetDateTime.now().plusHours(10);
    public static final OffsetDateTime SOME_NOT_VALID_INVITATION_DATE = OffsetDateTime.now().minusHours(73);
    public static final String USER_NAME_LONGER_256 = "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa";
    public static final String USER_NAME_EXACTLY_256 = "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa";
    // Credit Report signature
    public static final UUID SOME_CREDIT_REPORT_ID = UUID.fromString("8eeccc33-10ec-4b10-8e06-b94ec059baaa");

    public static final LegalDocument SOME_T_AND_C = new LegalDocument(randomUUID(), "<html>Some T&C content v3<html>", 999, LocalDate.now(), DocumentType.TERMS_AND_CONDITIONS);
    public static final LegalDocument SOME_PRIVACY_POLICY = new LegalDocument(randomUUID(), "<html>Some Privacy Policy v2<html>", 999, LocalDate.now(), DocumentType.PRIVACY_POLICY);
}
