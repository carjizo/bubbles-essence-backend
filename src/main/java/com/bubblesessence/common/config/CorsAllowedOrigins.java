package com.bubblesessence.common.config;

import org.springframework.stereotype.Component;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Component 
@ConfigurationProperties(prefix = "cors.allowed.origins")
public class CorsAllowedOrigins {
    private String origins;

    public String getOrigins() {
        return origins;
    }
    public void setOrigins(String origins) {
        this.origins = origins;
    }
}
