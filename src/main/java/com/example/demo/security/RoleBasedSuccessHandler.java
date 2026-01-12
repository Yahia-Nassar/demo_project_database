package com.example.demo.security;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class RoleBasedSuccessHandler implements AuthenticationSuccessHandler {

    @Override
    public void onAuthenticationSuccess(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication
    ) throws IOException, ServletException {

        if (authentication.getAuthorities()
                .contains(new SimpleGrantedAuthority("ROLE_PO"))) {
            response.sendRedirect("/po/dashboard");
            return;
        }

        if (authentication.getAuthorities()
                .contains(new SimpleGrantedAuthority("ROLE_DEVELOPER"))) {
            response.sendRedirect("/dev/dashboard");
            return;
        }

        response.sendRedirect("/login");
    }
}
