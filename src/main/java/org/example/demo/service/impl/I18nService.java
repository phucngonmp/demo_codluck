package org.example.demo.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpServletRequest;
import org.example.demo.service.II18nService;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.io.IOException;
import java.io.InputStream;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

@Service
public class I18nService implements II18nService {
    private static final String DEFAULT_LANGUAGE = "en";
    private static final String VI_LANGUAGE = "vi";

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final Map<String, Map<String, String>> messagesByLanguage = new HashMap<>();

    @PostConstruct
    public void init() throws IOException {
        messagesByLanguage.put(DEFAULT_LANGUAGE, loadMessages("i18n/en.json"));
        messagesByLanguage.put(VI_LANGUAGE, loadMessages("i18n/vi.json"));
    }

    @Override
    public String getMessage(String key, Object... args) {
        return getMessage(getCurrentLocale(), key, args);
    }

    @Override
    public String getMessage(Locale locale, String key, Object... args) {
        String language = normalizeLanguage(locale == null ? null : locale.getLanguage());
        String template = messagesByLanguage.getOrDefault(language, messagesByLanguage.get(DEFAULT_LANGUAGE)).get(key);

        if (template == null) {
            template = messagesByLanguage.getOrDefault(DEFAULT_LANGUAGE, Map.of()).getOrDefault(key, key);
        }

        return MessageFormat.format(template, args == null ? new Object[]{} : args);
    }

    @Override
    public Locale getCurrentLocale() {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes == null) {
            return Locale.ENGLISH;
        }

        HttpServletRequest request = attributes.getRequest();
        String langParam = request.getParameter("lang");
        if (langParam != null && !langParam.isBlank()) {
            return Locale.forLanguageTag(normalizeLanguage(langParam));
        }

        String acceptLanguage = request.getHeader("Accept-Language");
        if (acceptLanguage != null && !acceptLanguage.isBlank()) {
            return Locale.forLanguageTag(normalizeLanguage(Locale.forLanguageTag(acceptLanguage).getLanguage()));
        }

        return Locale.ENGLISH;
    }

    private Map<String, String> loadMessages(String path) throws IOException {
        try (InputStream inputStream = new ClassPathResource(path).getInputStream()) {
            Map<String, Object> rawMessages = objectMapper.readValue(inputStream, new TypeReference<>() {
            });
            Map<String, String> flattenedMessages = new HashMap<>();
            flatten("", rawMessages, flattenedMessages);
            return flattenedMessages;
        }
    }

    @SuppressWarnings("unchecked")
    private void flatten(String prefix, Map<String, Object> source, Map<String, String> target) {
        for (Map.Entry<String, Object> entry : source.entrySet()) {
            String key = prefix.isBlank() ? entry.getKey() : prefix + "." + entry.getKey();
            Object value = entry.getValue();

            if (value instanceof Map<?, ?> nestedMap) {
                flatten(key, (Map<String, Object>) nestedMap, target);
            } else {
                target.put(key, String.valueOf(value));
            }
        }
    }

    private String normalizeLanguage(String language) {
        if (language == null || language.isBlank()) {
            return DEFAULT_LANGUAGE;
        }
        return language.toLowerCase(Locale.ROOT).startsWith(VI_LANGUAGE) ? VI_LANGUAGE : DEFAULT_LANGUAGE;
    }
}
