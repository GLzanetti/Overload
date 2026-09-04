package br.com.overload.gatewayservice.security;

import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class JwtFilterConfig{

    @Bean
    public FilterRegistrationBean<JwtAuthFilter> jwtAuthFilterRegistration(RotasPublicasProperties rotasPublicasProperties, JwtService jwtService) {

        JwtAuthFilter jwtAuthFilter = new JwtAuthFilter(rotasPublicasProperties, jwtService);

        FilterRegistrationBean<JwtAuthFilter> registration = new FilterRegistrationBean<>();
        registration.setFilter(jwtAuthFilter);
        registration.addUrlPatterns("/*");
        return registration;
    }

}
