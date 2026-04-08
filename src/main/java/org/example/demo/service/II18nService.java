package org.example.demo.service;

import java.util.Locale;

public interface II18nService {
    String getMessage(String key, Object... args);

    String getMessage(Locale locale, String key, Object... args);

    Locale getCurrentLocale();
}
