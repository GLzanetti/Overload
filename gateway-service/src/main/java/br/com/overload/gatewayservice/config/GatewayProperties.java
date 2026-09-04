package br.com.overload.gatewayservice.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.Map;

@Configuration
@ConfigurationProperties(prefix = "gateway")
public class GatewayProperties {

    private Map<String, String> rotas;

    public Map<String, String> getRotas() {
        return rotas;
    }

    public void setRotas(Map<String, String> rotas) {
        this.rotas = rotas;
    }
}
