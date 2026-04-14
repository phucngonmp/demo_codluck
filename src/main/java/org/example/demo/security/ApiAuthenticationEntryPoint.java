package org.example.demo.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.example.demo.common.ApiResponse;
import org.example.demo.common.ErrorCode;
import org.example.demo.i18n.Translator;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class ApiAuthenticationEntryPoint implements AuthenticationEntryPoint {
    private final ObjectMapper objectMapper;
    private final Translator translator;

    public ApiAuthenticationEntryPoint(ObjectMapper objectMapper, Translator translator) {
        this.objectMapper = objectMapper;
        this.translator = translator;
    }

    @Override
    public void commence(
            HttpServletRequest request,
            HttpServletResponse response,
            AuthenticationException authException
    ) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setCharacterEncoding("UTF-8");
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);

        ApiResponse<Object> body = ApiResponse.error(
                ErrorCode.UNAUTHORIZED,
                translator.get(ErrorCode.UNAUTHORIZED.getMessageKey())
        );

        objectMapper.writeValue(response.getWriter(), body);
    }
}
