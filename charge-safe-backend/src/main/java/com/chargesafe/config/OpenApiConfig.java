package com.chargesafe.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * OpenAPI / Knife4j 全局文档配置
 *
 * <p>统一 API 文档元信息，并声明 JWT Bearer 认证方案，使 doc.html 在调试时
 * 可在全局填入 Token，无需每个接口手动添加 Authorization 头。
 */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI chargeSafeOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("ChargeSafe 社区充电安全运维平台 API")
                        .description("面向社区场景的电动自行车充电停放与安全运维管理系统。\n\n" +
                                "统一返回结构：{ code: 200, message: \"success\", data: {} }；" +
                                "code=200 表示成功，非 200 表示业务异常。\n" +
                                "认证方式：JWT，请求头 Authorization: Bearer {token}。")
                        .version("v0.8.1")
                        .contact(new Contact()
                                .name("ChargeSafe Team")
                                .url("https://github.com/wangsf134/charge-safe"))
                        .license(new License().name("MIT").url("https://opensource.org/licenses/MIT")))
                .components(new Components()
                        .addSecuritySchemes("BearerAuth", new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .description("JWT Token，格式：Bearer {token}")));
    }
}
