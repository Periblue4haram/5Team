package com.example.myeongranghoe.config;

import com.example.myeongranghoe.service.AuthSessionService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class AuthInterceptor implements HandlerInterceptor {
    private final AuthSessionService authSessionService;
    private final boolean allowEmailHeader;

    public AuthInterceptor(
            AuthSessionService authSessionService,
            @Value("${app.auth.allow-email-header:true}") boolean allowEmailHeader) {
        this.authSessionService = authSessionService;
        this.allowEmailHeader = allowEmailHeader;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String email = null;
        String auth = request.getHeader("Authorization");
        if (auth != null && auth.regionMatches(true, 0, "Bearer ", 0, 7)) {
            email = authSessionService.resolveEmail(auth.substring(7).trim()).orElse(null);
        }
        if (email == null && allowEmailHeader) {
            String headerEmail = request.getHeader("X-User-Email");
            if (headerEmail != null && !headerEmail.isBlank()) {
                email = headerEmail.trim().toLowerCase();
            }
        }
        if (email != null) {
            UserContext.set(email);
        }
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        UserContext.clear();
    }
}
