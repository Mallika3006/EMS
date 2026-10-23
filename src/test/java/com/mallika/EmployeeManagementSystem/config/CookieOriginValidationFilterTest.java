package com.mallika.EmployeeManagementSystem.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

class CookieOriginValidationFilterTest {

    private CookieOriginValidationFilter filter;

    @BeforeEach
    void setUp() {
        filter = new CookieOriginValidationFilter();
        ReflectionTestUtils.setField(
                filter, "allowedOrigin", "http://localhost:8080");
    }

    @Test
    void allowsSameOriginUnsafeRequestWithJwtCookie() throws ServletException, IOException {
        MockHttpServletRequest request = requestWithJwtCookie();
        request.setMethod("POST");
        request.addHeader("Origin", "http://localhost:8080");

        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();

        filter.doFilter(request, response, chain);

        assertEquals(200, response.getStatus());
        assertNotNull(chain.getRequest());
    }

    @Test
    void rejectsCrossOriginUnsafeRequestWithJwtCookie() throws ServletException, IOException {
        MockHttpServletRequest request = requestWithJwtCookie();
        request.setMethod("DELETE");
        request.addHeader("Origin", "https://attacker.example");

        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();

        filter.doFilter(request, response, chain);

        assertEquals(403, response.getStatus());
        assertNull(chain.getRequest());
    }

    @Test
    void rejectsUnsafeCookieRequestWithoutOriginOrReferer() throws ServletException, IOException {
        MockHttpServletRequest request = requestWithJwtCookie();
        request.setMethod("PUT");

        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();

        filter.doFilter(request, response, chain);

        assertEquals(403, response.getStatus());
        assertNull(chain.getRequest());
    }

    @Test
    void allowsExplicitBearerApiRequestWithoutBrowserOrigin() throws ServletException, IOException {
        MockHttpServletRequest request = requestWithJwtCookie();
        request.setMethod("POST");
        request.addHeader("Authorization", "Bearer api-token");

        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();

        filter.doFilter(request, response, chain);

        assertEquals(200, response.getStatus());
        assertNotNull(chain.getRequest());
    }

    private MockHttpServletRequest requestWithJwtCookie() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setCookies(new Cookie("jwt", "test-jwt"));
        return request;
    }
}
