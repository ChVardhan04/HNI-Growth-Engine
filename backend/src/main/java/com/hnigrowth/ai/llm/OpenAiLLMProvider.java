package com.hnigrowth.ai.llm;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@Component
@ConditionalOnProperty(
        name = "app.ai.provider",
        havingValue = "openai"
)
public class OpenAiLLMProvider implements LLMProvider {

    @Value("${app.ai.base-url}")
    private String baseUrl;

    @Value("${app.ai.api-key}")
    private String apiKey;

    @Value("${app.ai.model}")
    private String model;

    private final RestTemplate restTemplate = new RestTemplate();

    private final ObjectMapper mapper = new ObjectMapper();

    @Override
    public String complete(String prompt) {

        HttpHeaders headers = new HttpHeaders();

        headers.setBearerAuth(apiKey);

        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, Object> body = Map.of(

                "model", model,

                "messages", List.of(

                        Map.of(

                                "role", "user",

                                "content", prompt

                        )

                )

        );

        HttpEntity<Map<String, Object>> request =

                new HttpEntity<>(body, headers);

        ResponseEntity<String> response = restTemplate.exchange(

                baseUrl + "/chat/completions",

                HttpMethod.POST,

                request,

                String.class

        );

        try {

            JsonNode json = mapper.readTree(response.getBody());

            return json

                    .get("choices")

                    .get(0)

                    .get("message")

                    .get("content")

                    .asText();

        }

        catch (Exception e) {

            throw new RuntimeException("Unable to parse AI response", e);

        }

    }

    @Override
    public String name() {

        return "openai";

    }

}