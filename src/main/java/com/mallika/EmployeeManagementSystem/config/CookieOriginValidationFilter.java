package com.mallika.EmployeeManagementSystem.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Set;

@Component
public class CookieOriginValidationFilter extends OncePerRequestFilter {

    private static final Set<String> SAFE_METHODS =
            Set.of("GET", "HEAD", "OPTIONS", "TRACE");

    @Value("${app.security.allowed-origin:http://localhost:8080}")
    private String allowedOrigin;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {

        if (SAFE_METHODS.contains(request.getMethod().toUpperCase())) {
            filterChain.doFilter(request, response);
            return;
        }

        // Bearer tokens are explicit API credentials, not ambient browser cookies.
        String authorization = request.getHeader("Authorization");
        if (authorization != null
                && authorization.regionMatches(true, 0, "Bearer ", 0, 7)) {
            filterChain.doFilter(request, response);
            return;
        }

        if (!hasJwtCookie(request)) {
            filterChain.doFilter(request, response);
            return;
        }

        String requestOrigin = request.getHeader("Origin");
        if (requestOrigin == null || requestOrigin.isBlank()) {
            String referer = request.getHeader("Referer");
            if (referer != null && !referer.isBlank()) {
                try {
                    URI refererUri = new URI(referer);
                    requestOrigin = refererUri.getScheme() + "://"
                            + refererUri.getRawAuthority();
                } catch (URISyntaxException ignored) {
                    requestOrigin = null;
                }
            }
        }

        if (!sameOrigin(requestOrigin, allowedOrigin)) {
            response.sendError(
                    HttpServletResponse.SC_FORBIDDEN,
                    "Cross-origin cookie-authenticated request rejected");
            return;
        }

        filterChain.doFilter(request, response);
    }

    private boolean hasJwtCookie(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null) {
            return false;
        }
        for (Cookie cookie : cookies) {
            if ("jwt".equals(cookie.getName()) && cookie.getValue() != null
                    && !cookie.getValue().isBlank()) {
                return true;
            }
        }
        return false;
    }

    private boolean sameOrigin(String suppliedOrigin, String configuredOrigin) {
        if (suppliedOrigin == null || suppliedOrigin.isBlank()
                || "null".equalsIgnoreCase(suppliedOrigin)) {
            return false;
        }
        try {
            URI supplied = new URI(suppliedOrigin);
            URI configured = new URI(configuredOrigin);
            if (supplied.getScheme() == null || supplied.getHost() == null
                    || configured.getScheme() == null || configured.getHost() == null
                    || supplied.getRawUserInfo() != null
                    || configured.getRawUserInfo() != null
                    || supplied.getRawQuery() != null || supplied.getRawFragment() != null
                    || configured.getRawQuery() != null || configured.getRawFragment() != null) {
                return false;
            }
            return supplied.getScheme().equalsIgnoreCase(configured.getScheme())
                    && supplied.getHost().equalsIgnoreCase(configured.getHost())
                    && effectivePort(supplied) == effectivePort(configured)
                    && (supplied.getRawPath() == null || supplied.getRawPath().isEmpty()
                        || "/".equals(supplied.getRawPath()))
                    && (configured.getRawPath() == null || configured.getRawPath().isEmpty()
                        || "/".equals(configured.getRawPath()));
        } catch (URISyntaxException e) {
            return false;
        }
    }

    private int effectivePort(URI uri) {
        if (uri.getPort() >= 0) {
            return uri.getPort();
        }
        return "https".equalsIgnoreCase(uri.getScheme()) ? 443 : 80;
    }
}
