package com.hnigrowth.linkedin.config;

import org.springframework.boot.web.client.ClientHttpRequestFactories;
import org.springframework.boot.web.client.ClientHttpRequestFactorySettings;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

import java.time.Duration;

/**
 * Linked API (linkedapi.io) does NOT use Bearer auth -- it requires two
 * custom headers ("linked-api-token" and "identification-token") on every
 * request. Those are attached per-call in LinkedInApiClient rather than as a
 * client default, since identification-token is account-specific.
 */
@Configuration
public class RestClientConfig {

    @Bean
    public RestClient linkedInRestClient(LinkedInApiProperties props) {
        // Without explicit timeouts the underlying HTTP client will hang on a slow/dead
        // connection for a very long time (OS-level default), which is why campaign
        // launches used to appear to "start but never finish". Fail fast instead so
        // LinkedInWorkflowExecutor's own error handling/logging kicks in quickly.
        ClientHttpRequestFactorySettings settings = ClientHttpRequestFactorySettings.DEFAULTS
                .withConnectTimeout(Duration.ofSeconds(10))
                .withReadTimeout(Duration.ofSeconds(30));
        RestClient.Builder builder = RestClient.builder()
                .requestFactory(ClientHttpRequestFactories.get(settings));
        if (props.getBaseUrl() != null && !props.getBaseUrl().isBlank()) {
            builder.baseUrl(props.getBaseUrl());
        }
        return builder.build();
    }
}
