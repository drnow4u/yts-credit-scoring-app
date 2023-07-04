package com.yolt.creditscoring.configuration;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.function.Supplier;

import static java.util.UUID.randomUUID;

@Configuration
public class InvitationHashConfiguration {

    @Bean
    @Qualifier("InvitationHashSupplier")
    public Supplier<String> createInvitationHashSupplier() {
        return () -> randomUUID().toString();
    }
}
