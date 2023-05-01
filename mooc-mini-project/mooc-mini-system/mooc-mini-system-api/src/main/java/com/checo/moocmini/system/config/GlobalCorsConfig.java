package com.checo.moocmini.system.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

@Configuration
public class GlobalCorsConfig {

    @Bean
    public CorsFilter corsFilter() {
        CorsConfiguration corsConfiguration = new CorsConfiguration();
        // 允许白名单域进行跨域调用
        corsConfiguration.addAllowedOrigin("*");
        // 允许跨域发送 Cookie
        corsConfiguration.setAllowCredentials(true);
        // 放行全部原始头信息
        corsConfiguration.addAllowedHeader("*");
        // 允许所有请求方法跨域调用
        corsConfiguration.addAllowedMethod("*");
        UrlBasedCorsConfigurationSource corsConfigurationSource = new UrlBasedCorsConfigurationSource();
        corsConfigurationSource.registerCorsConfiguration("/**", corsConfiguration);
        return new CorsFilter(corsConfigurationSource);
    }
}
