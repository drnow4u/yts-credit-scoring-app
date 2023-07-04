package com.yolt.creditscoring.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.thymeleaf.ITemplateEngine;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;
import org.thymeleaf.templateresolver.ITemplateResolver;

import java.util.Collections;

@Configuration
public class ThymeleafConfiguration {

    @Bean
    public ITemplateEngine templateEngine() {
        TemplateEngine templateEngine = new TemplateEngine();
        templateEngine.addTemplateResolver(textTemplateResolver());
        templateEngine.addTemplateResolver(htmlTemplateResolver());

        return templateEngine;
    }

    @Bean
    public ITemplateResolver textTemplateResolver() {
        ClassLoaderTemplateResolver templateResolver = new ClassLoaderTemplateResolver();
        templateResolver.setOrder(1);
        templateResolver.setResolvablePatterns(Collections.singleton("text/*"));
        templateResolver.setPrefix("/mail/");
        templateResolver.setSuffix(".txt");
        templateResolver.setTemplateMode(TemplateMode.TEXT);
        templateResolver.setCacheable(false);

        return templateResolver;
    }

    @Bean
    public ITemplateResolver htmlTemplateResolver() {
        ClassLoaderTemplateResolver templateResolver = new ClassLoaderTemplateResolver();
        templateResolver.setOrder(2);
        templateResolver.setResolvablePatterns(Collections.singleton("html/*"));
        templateResolver.setPrefix("/mail/");
        templateResolver.setSuffix(".html");
        templateResolver.setTemplateMode(TemplateMode.HTML);
        templateResolver.setCacheable(false);

        return templateResolver;
    }
}
