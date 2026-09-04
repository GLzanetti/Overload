package br.com.overload.gatewayservice.security;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
@ConfigurationProperties(prefix = "gateway")
public class RotasPublicasProperties {

    private List<String> rotasPublicas;

    public List<String> getRotasPublicas() {
        return rotasPublicas;
    }

    public void setRotasPublicas(List<String> rotasPublicas) {
        this.rotasPublicas = rotasPublicas;
    }
}
