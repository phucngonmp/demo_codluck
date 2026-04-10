package org.example.demo.i18n;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.support.AbstractMessageSource;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;
import java.io.InputStream;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

// cấu hình custom vì mặc định là spring là tìm message trong message.properties
public class JsonMessageSource extends AbstractMessageSource {
    private static final Locale DEFAULT_LOCALE = Locale.ENGLISH;
    private static final Set<String> SUPPORTED_LANGUAGES = Set.of("en", "vi");

    private final Map<String, Map<String, String>> messagesByLanguage = new HashMap<>();

    public JsonMessageSource(ObjectMapper objectMapper) {
        loadLanguageFile(objectMapper, "en");
        loadLanguageFile(objectMapper, "vi");
        setUseCodeAsDefaultMessage(true);
    }

    @Override
    protected MessageFormat resolveCode(String code, Locale locale) {
        String message = resolveCodeWithoutArguments(code, locale);
        return message == null ? null : new MessageFormat(message, normalizeLocale(locale));
    }

    @Override
    protected String resolveCodeWithoutArguments(String code, Locale locale) {
        Locale normalizedLocale = normalizeLocale(locale);
        Map<String, String> localizedMessages = messagesByLanguage.get(normalizedLocale.getLanguage());

        if (localizedMessages != null && localizedMessages.containsKey(code)) {
            return localizedMessages.get(code);
        }

        Map<String, String> defaultMessages = messagesByLanguage.get(DEFAULT_LOCALE.getLanguage());
        return defaultMessages == null ? null : defaultMessages.get(code);
    }

    private Locale normalizeLocale(Locale locale) {
        if (locale == null) {
            return DEFAULT_LOCALE;
        }

        String language = locale.getLanguage();
        if (!SUPPORTED_LANGUAGES.contains(language)) {
            return DEFAULT_LOCALE;
        }

        return Locale.forLanguageTag(language);
    }

    private void loadLanguageFile(ObjectMapper objectMapper, String language) {
        ClassPathResource resource = new ClassPathResource("i18n/" + language + ".json");
        try (InputStream inputStream = resource.getInputStream()) {
            Map<String, Object> source = objectMapper.readValue(inputStream, new TypeReference<>() {});
            Map<String, String> flattened = new LinkedHashMap<>();
            flatten("", source, flattened);
            messagesByLanguage.put(language, flattened);
        } catch (IOException exception) {
            throw new IllegalStateException("Unable to load i18n file for language: " + language, exception);
        }
    }

    @SuppressWarnings("unchecked")
    private void flatten(String prefix, Map<String, Object> source, Map<String, String> output) {
        for (Map.Entry<String, Object> entry : source.entrySet()) {
            String key = prefix.isEmpty() ? entry.getKey() : prefix + "." + entry.getKey();
            Object value = entry.getValue();

            if (value instanceof Map<?, ?> nestedMap) {
                flatten(key, (Map<String, Object>) nestedMap, output);
                continue;
            }

            output.put(key, value == null ? "" : value.toString());
        }
    }
}
