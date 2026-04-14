package org.example.demo.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.example.demo.common.ApiResponse;
import org.example.demo.common.ErrorCode;
import org.example.demo.i18n.Translator;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class ApiAccessDeniedHandler implements AccessDeniedHandler {
    private final ObjectMapper objectMapper;
    private final Translator translator;

    public ApiAccessDeniedHandler(ObjectMapper objectMapper, Translator translator) {
        this.objectMapper = objectMapper;
        this.translator = translator;
    }

    @Override
    public void handle(
            HttpServletRequest request,
            HttpServletResponse response,
            AccessDeniedException accessDeniedException
    ) throws IOException {
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        response.setCharacterEncoding("UTF-8");
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);

        ApiResponse<Object> body = ApiResponse.error(
                ErrorCode.FORBIDDEN,
                translator.get(ErrorCode.FORBIDDEN.getMessageKey())
        );

        objectMapper.writeValue(response.getWriter(), body);
    }
}
