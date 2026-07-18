package com.chargesafe.config;

import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.servlet.config.annotation.CorsRegistry;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class WebConfigTest {

    @Test
    void rejectsWildcardOriginWhenCookiesAreAllowed() {
        WebConfig config = new WebConfig();
        ReflectionTestUtils.setField(config, "allowedOrigins", "*");

        assertThrows(IllegalArgumentException.class,
                () -> config.addCorsMappings(new CorsRegistry()));
    }

    @Test
    void acceptsAnExplicitDevelopmentOrigin() {
        WebConfig config = new WebConfig();
        ReflectionTestUtils.setField(config, "allowedOrigins", "http://localhost:3000");

        assertDoesNotThrow(() -> config.addCorsMappings(new CorsRegistry()));
    }
}
