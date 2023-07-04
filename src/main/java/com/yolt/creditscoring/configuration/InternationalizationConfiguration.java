package com.yolt.creditscoring.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.i18n.FixedLocaleResolver;

import java.util.Locale;

/**
 * Error message translation is handled by the front-end.
 * Backend should only provide error code with parameters required to render correctly message for user in GUI.
 */
@Configuration
public class InternationalizationConfiguration {
    @Bean
    public LocaleResolver localeResolver() {
        //TODO: Null Object of MessageSource should be used instead. Null Object should return message code. Not translation to Locale.US.
        // Solution could be to provide custom configuration instead of MessageSourceAutoConfiguration
        // I try to set properties spring.messages.useCodeAsDefaultMessage=true instead of this. Tests pass from Maven, but not run for Intellij.
        FixedLocaleResolver localeResolver = new FixedLocaleResolver();
        localeResolver.setDefaultLocale(Locale.US);
        return localeResolver;
    }
}
