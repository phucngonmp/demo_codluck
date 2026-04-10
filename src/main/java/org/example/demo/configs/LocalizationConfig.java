package org.example.demo.configs;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.demo.i18n.JsonMessageSource;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.i18n.AcceptHeaderLocaleResolver;
import org.springframework.web.servlet.i18n.LocaleChangeInterceptor;

import java.util.List;
import java.util.Locale;

@Configuration
public class LocalizationConfig {
    @Bean
    public ObjectMapper objectMapper() {
        return new ObjectMapper();
    }


    // source của message
    @Bean
    public MessageSource messageSource(ObjectMapper objectMapper) {
        return new JsonMessageSource(objectMapper);
    }


    // dùng cho validation message của @Valid validation ha
    @Bean
    public LocalValidatorFactoryBean validator(MessageSource messageSource) {
        LocalValidatorFactoryBean validatorFactoryBean = new LocalValidatorFactoryBean();
        validatorFactoryBean.setValidationMessageSource(messageSource);
        return validatorFactoryBean;
    }


    // xác định locale của request thông qua accept header từ header ca request
    @Bean
    public LocaleResolver localeResolver() {
        AcceptHeaderLocaleResolver localeResolver = new AcceptHeaderLocaleResolver();
        localeResolver.setSupportedLocales(List.of(Locale.ENGLISH, Locale.forLanguageTag("vi")));
        localeResolver.setDefaultLocale(Locale.forLanguageTag("vi"));
        return localeResolver;
    }
}
