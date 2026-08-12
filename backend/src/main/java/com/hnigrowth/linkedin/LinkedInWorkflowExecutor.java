package com.hnigrowth.linkedin;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hnigrowth.linkedin.config.LinkedInApiProperties;
import com.hnigrowth.linkedin.exceptions.LinkedInApiException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.Map;

/**
 * Executes Linked API (linkedapi.io) workflows: POST /workflows to start one
 * (returns a workflowId), then poll GET /workflows/{id} until it reports
 * "completed" or "failed". See https://linkedapi.io/docs/executing-workflows/
 *
 * Linked API fully emulates a real LinkedIn user, so workflows are NOT
 * instant -- simple ones take ~20s, complex ones (e.g. company + employee
 * enrichment) can take minutes. This executor polls synchronously with a
 * bounded timeout; for production traffic, prefer Linked API's webhook
 * events instead of polling on the request thread (see Webhook Events in
 * their docs) so campaign launches don't hold an HTTP connection open.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class LinkedInWorkflowExecutor {

    private static final String WORKFLOWS_PATH = "/workflows";
    private static final int POLL_INTERVAL_MS = 3000;
    private static final int MAX_POLL_ATTEMPTS = 60; // ~3 minutes max wait

    private final RestClient linkedInRestClient;
    private final LinkedInApiProperties props;
    private final ObjectMapper objectMapper = new ObjectMapper();

    /** Starts the given workflow and blocks (with polling) until it completes or fails. */
    public JsonNode executeAndWait(Map<String, Object> workflow) {
        String workflowId = start(workflow);
        return poll(workflowId);
    }

    private String start(Map<String, Object> workflow) {
        try {
            String body = linkedInRestClient.post()
                    .uri(WORKFLOWS_PATH)
                    .headers(this::authHeaders)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(workflow)
                    .retrieve()
                    .body(String.class);
            JsonNode root = objectMapper.readTree(body);
            if (!root.path("success").asBoolean(false)) {
                throw new LinkedInApiException("Linked API rejected workflow: " + root.path("error").toString());
            }
            String workflowId = root.path("result").path("workflowId").asText(null);
            if (workflowId == null) {
                throw new LinkedInApiException("Linked API did not return a workflowId: " + body);
            }
            return workflowId;
        } catch (LinkedInApiException e) {
            throw e;
        } catch (Exception e) {
            // Keep e's own message in the thrown exception -- callers/logs currently only
            // print getMessage(), and a bare "Failed to start Linked API workflow" hides
            // the actual cause (401 unauthorized, DNS/connect failure, timeout, etc.).
            throw new LinkedInApiException("Failed to start Linked API workflow: " + e.getClass().getSimpleName()
                    + (e.getMessage() != null ? " - " + e.getMessage() : ""), e);
        }
    }

    private JsonNode poll(String workflowId) {
        for (int attempt = 0; attempt < MAX_POLL_ATTEMPTS; attempt++) {
            try {
                String body = linkedInRestClient.get()
                        .uri(WORKFLOWS_PATH + "/{id}", workflowId)
                        .headers(this::authHeaders)
                        .retrieve()
                        .body(String.class);
                JsonNode root = objectMapper.readTree(body);
                String status = root.path("result").path("workflowStatus").asText("");
                if ("completed".equals(status)) {
                    JsonNode completion = root.path("result").path("completion");
                    // "completed" only means Linked API finished running the workflow --
                    // the action itself can still have failed (e.g. unexpectedError).
                    // Treat that as a real failure instead of silently returning it as
                    // if it were an empty-but-successful result.
                    if (!completion.path("success").asBoolean(true)) {
                        JsonNode error = completion.path("error");
                        throw new LinkedInApiException("Linked API action failed ["
                                + error.path("type").asText("unknown") + "]: "
                                + error.path("message").asText("no message provided"));
                    }
                    return completion;
                }
                if ("failed".equals(status)) {
                    throw new LinkedInApiException("Linked API workflow failed: "
                            + root.path("result").path("failure").toString());
                }
                Thread.sleep(POLL_INTERVAL_MS);
            } catch (InterruptedException ie) {
                Thread.currentThread().interrupt();
                throw new LinkedInApiException("Interrupted while polling Linked API workflow " + workflowId, ie);
            } catch (LinkedInApiException e) {
                throw e;
            } catch (Exception e) {
                throw new LinkedInApiException("Failed to poll Linked API workflow " + workflowId, e);
            }
        }
        throw new LinkedInApiException("Linked API workflow " + workflowId + " did not complete within timeout");
    }

    private void authHeaders(HttpHeaders headers) {
        headers.set("linked-api-token", props.getApiKey());
        headers.set("identification-token", props.getAccountId());
    }
}
