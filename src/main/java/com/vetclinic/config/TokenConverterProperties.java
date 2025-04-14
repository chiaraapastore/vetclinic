package com.vetclinic.config;

import org.springframework.context.annotation.Configuration;

@Configuration
public class TokenConverterProperties {

    private String resourceId;
    private String principalAttribute;

    public String getResourceId() {
        return resourceId;
    }



}
