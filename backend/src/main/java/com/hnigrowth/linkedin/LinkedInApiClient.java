package com.hnigrowth.linkedin;

import com.fasterxml.jackson.databind.JsonNode;
import com.hnigrowth.linkedin.config.LinkedInApiProperties;
import com.hnigrowth.linkedin.dto.LinkedInMessageRequest;
import com.hnigrowth.linkedin.dto.LinkedInProfileDto;
import com.hnigrowth.linkedin.dto.LinkedInSearchRequest;
import com.hnigrowth.linkedin.dto.LinkedInSendResult;
import com.hnigrowth.linkedin.exceptions.LinkedInApiException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Thin client wrapping Linked API (linkedapi.io). This is the ONLY class that
 * builds/sends Linked API workflow payloads. Controllers/services must never
 * call this directly -- go through LinkedInService.
 *
 * Linked API reference: https://linkedapi.io/docs/
 * - Auth: headers "linked-api-token" + "identification-token" (see LinkedInWorkflowExecutor)
 * - Search: st.searchPeople action, nested with st.doForPeople -> st.openPersonPage
 *   to enrich each hit with headline/company/location in a single workflow.
 * - Outreach: st.sendConnectionRequest (personUrl, note) / st.sendMessage (personUrl, text)
 *
 * When linkedin.api.mock-mode is true (default) or credentials are missing,
 * every call returns deterministic mock data so the rest of the platform
 * (enrichment, scoring, approval, messaging) is fully exercisable without a
 * live LinkedIn account.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class LinkedInApiClient {

    private final LinkedInWorkflowExecutor workflowExecutor;
    private final LinkedInApiProperties props;

    public List<LinkedInProfileDto> search(LinkedInSearchRequest request) {
        if (!props.isConfigured() || props.isMockMode()) {
            return mockSearch(request);
        }
        try {
            Map<String, Object> workflow = buildSearchWorkflow(request);
            JsonNode completion = workflowExecutor.executeAndWait(workflow);
            List<LinkedInProfileDto> profiles = parseSearchCompletion(completion);
            log.info("[linkedin] live search returned {} profiles", profiles.size());
            return profiles;
        } catch (LinkedInApiException e) {
            // Credentials ARE configured and mock-mode is off, so this is a genuine
            // live-mode failure -- surface it instead of quietly substituting fake
            // "Mock Lead" rows, which would hide a real integration problem from the
            // RM launching the campaign. The caller (LinkedInWorkflowService /
            // CampaignService) is responsible for marking the campaign FAILED and
            // showing this message, not silently reporting fabricated results.
            log.error("[linkedin] live search failed: {}", e.getMessage(), e);
            throw e;
        }
    }

    /** Checks whether the given LinkedIn profile is a 1st-degree connection,
     * has a pending connection request, or is not connected at all. Direct
     * messages can only be sent to CONNECTED profiles -- this MUST be checked
     * before st.sendMessage, since LinkedIn silently ignores/rejects messages
     * to non-connections (there's no InMail path wired here). */
    public com.hnigrowth.linkedin.dto.ConnectionStatus checkConnectionStatus(String personUrl) {
        if (!props.isConfigured() || props.isMockMode()) {
            // Deterministic mock behaviour: mock leads are never "connected" until
            // a connection request has been (mock-)sent, so the first send attempt
            // always exercises the auto-connect path.
            return com.hnigrowth.linkedin.dto.ConnectionStatus.NOT_CONNECTED;
        }
        try {
            Map<String, Object> workflow = new LinkedHashMap<>();
            workflow.put("actionType", "st.checkConnectionStatus");
            workflow.put("personUrl", personUrl);
            JsonNode completion = workflowExecutor.executeAndWait(workflow);
            String status = completion.path("data").path("connectionStatus").asText("");
            return switch (status) {
                case "connected" -> com.hnigrowth.linkedin.dto.ConnectionStatus.CONNECTED;
                case "pending" -> com.hnigrowth.linkedin.dto.ConnectionStatus.PENDING;
                case "notConnected" -> com.hnigrowth.linkedin.dto.ConnectionStatus.NOT_CONNECTED;
                default -> com.hnigrowth.linkedin.dto.ConnectionStatus.UNKNOWN;
            };
        } catch (LinkedInApiException e) {
            log.warn("[linkedin] connection status check failed for {}: {}", personUrl, e.getMessage());
            return com.hnigrowth.linkedin.dto.ConnectionStatus.UNKNOWN;
        }
        // NOTE: the exact response shape for st.checkConnectionStatus isn't confirmed
        // against a real Linked API response (unlike search/openPersonPage, which we
        // validated against your live test data). This mapping is a best-effort read
        // of their documented action list; if live testing shows a different field
        // name/value set, update the switch above accordingly.
    }

    public LinkedInSendResult sendMessage(LinkedInMessageRequest request) {
        if (!props.isConfigured() || props.isMockMode()) {
            return mockSend(request);
        }
        try {
            Map<String, Object> workflow = buildSendWorkflow(request);
            JsonNode completion = workflowExecutor.executeAndWait(workflow);
            boolean success = completion.path("success").asBoolean(false);
            if (success) {
                return LinkedInSendResult.ok(UUID.randomUUID().toString(), "SENT");
            }
            String error = completion.path("error").path("message").asText("Unknown Linked API error");
            return LinkedInSendResult.failed(error);
        } catch (LinkedInApiException e) {
            return LinkedInSendResult.failed(e.getMessage());
        }
    }

    // --- Real workflow construction -----------------------------------------------------

    private Map<String, Object> buildSearchWorkflow(LinkedInSearchRequest request) {
        // NOTE: customSearchUrl is only documented for nv.searchPeople (Sales Navigator)
        // and st.searchCompanies -- NOT for st.searchPeople (standard LinkedIn search),
        // which is what we use here. Passing it to st.searchPeople appears to be
        // silently ignored, which produced an empty-term search with zero results.
        // term/filter is therefore always used for the standard search; customSearchUrl
        // is preserved on the Campaign for a future nv.searchPeople (Sales Navigator)
        // code path where it's actually supported.
        //
        // IMPORTANT: do NOT nest st.doForPeople -> st.openPersonPage here. A plain
        // st.searchPeople call already returns name/headline/location/publicUrl/
        // avatarUrl per hit (verified against Linked API's own example response).
        // Nesting an openPersonPage visit for every hit forces Linked API to open
        // each profile as a real, human-speed page load -- for a limit of 25 that
        // is 25 sequential ~20s+ actions, which blows past the 3-minute workflow
        // poll timeout in LinkedInWorkflowExecutor and makes the whole campaign
        // launch hang and then fail. Enrichment (about/companyName/full profile)
        // should be a separate, per-lead, on-demand action -- e.g. triggered when
        // an RM opens a specific lead -- not bundled into every bulk search.
        Map<String, Object> filter = new LinkedHashMap<>();
        if (request.designation() != null) filter.put("position", request.designation());
        if (request.location() != null) filter.put("locations", List.of(request.location()));
        if (request.companyName() != null) filter.put("currentCompanies", List.of(request.companyName()));

        Map<String, Object> workflow = new LinkedHashMap<>();
        workflow.put("actionType", "st.searchPeople");
        workflow.put("term", buildSearchTerm(request));
        workflow.put("limit", request.maxResults() == null ? 25 : Math.min(request.maxResults(), 50));
        if (!filter.isEmpty()) workflow.put("filter", filter);
        return workflow;
    }

    /** Separate, single-profile enrichment workflow. Call this on demand (e.g. when an
     * RM opens a lead's detail view) rather than for every hit in a bulk search. */
    private Map<String, Object> buildOpenProfileWorkflow(String personUrl) {
        Map<String, Object> workflow = new LinkedHashMap<>();
        workflow.put("actionType", "st.openPersonPage");
        workflow.put("personUrl", personUrl);
        workflow.put("basicInfo", true);
        return workflow;
    }

    private String buildSearchTerm(LinkedInSearchRequest request) {
        List<String> parts = new ArrayList<>();
        if (request.designation() != null) parts.add(request.designation());
        if (request.industry() != null) parts.add(request.industry());
        if (request.keywords() != null) parts.addAll(request.keywords());
        return parts.isEmpty() ? "" : String.join(" ", parts);
    }

    private Map<String, Object> buildSendWorkflow(LinkedInMessageRequest request) {
        Map<String, Object> workflow = new LinkedHashMap<>();
        String personUrl = request.recipientProviderProfileId();
        switch (request.type()) {
            case CONNECTION_REQUEST -> {
                workflow.put("actionType", "st.sendConnectionRequest");
                workflow.put("personUrl", personUrl);
                workflow.put("note", request.message());
            }
            case DIRECT_MESSAGE, FOLLOW_UP -> {
                workflow.put("actionType", "st.sendMessage");
                workflow.put("personUrl", personUrl);
                workflow.put("text", request.message());
            }
        }
        return workflow;
    }

    // --- Real response parsing -----------------------------------------------------------

    /** Parses a plain st.searchPeople completion. Linked API returns each hit flat --
     * name, headline, location, publicUrl, avatarUrl -- with no enrichment step needed. */
    private List<LinkedInProfileDto> parseSearchCompletion(JsonNode completion) {
        List<LinkedInProfileDto> profiles = new ArrayList<>();
        for (JsonNode hit : completion.path("data")) {
            String name = text(hit, "name");
            String headline = text(hit, "headline");
            String location = text(hit, "location");
            String publicUrl = text(hit, "publicUrl");
            String avatarUrl = text(hit, "avatarUrl");

            profiles.add(new LinkedInProfileDto(
                    publicUrl,
                    name,
                    headline,
                    null, // company name isn't in the flat search payload; needs a per-lead enrichment call
                    null, // same for current position -- headline is the closest proxy here
                    location,
                    null, // Linked API does not return years-of-experience in search results
                    List.of(),
                    List.of(),
                    publicUrl,
                    avatarUrl,
                    null,
                    null,
                    null,
                    null
            ));
        }
        return profiles;
    }

    private String text(JsonNode node, String field) {
        JsonNode v = node.path(field);
        return v.isMissingNode() || v.isNull() ? null : v.asText();
    }

    // --- Mock-mode implementation (MOCK_MODE / unconfigured credentials) ---

    private List<LinkedInProfileDto> mockSearch(LinkedInSearchRequest request) {
        int count = request.maxResults() == null ? 10 : Math.min(request.maxResults(), 25);
        return java.util.stream.IntStream.range(0, count)
                .mapToObj(i -> mockProfile(request, i))
                .toList();
    }

    private LinkedInProfileDto mockProfile(LinkedInSearchRequest request, int i) {
        String designation = request.designation() != null ? request.designation() : "Director";
        String industry = request.industry() != null ? request.industry() : "Finance";
        String location = request.location() != null ? request.location() : "Mumbai";
        String company = request.companyName() != null ? request.companyName() : "Company " + (i + 1);
        int exp = ThreadLocalRandom.current().nextInt(
                request.minExperience() == null ? 5 : request.minExperience(),
                Math.max((request.maxExperience() == null ? 25 : request.maxExperience()) + 1,
                        (request.minExperience() == null ? 5 : request.minExperience()) + 1));
        String id = "mock-" + UUID.randomUUID();
        return new LinkedInProfileDto(
                id,
                "Mock Lead " + (i + 1),
                designation + " at " + company,
                company,
                designation,
                location,
                exp,
                List.of("MBA, Indian Institute of Management"),
                request.keywords() == null || request.keywords().isEmpty()
                        ? List.of("Wealth Management") : request.keywords(),
                "https://linkedin.com/in/" + id,
                "https://placehold.co/128x128?text=" + (i + 1),
                industry,
                request.companySize() != null ? request.companySize() : "201-500",
                i % 3 == 0 ? "2nd" : "3rd",
                "Experienced " + designation.toLowerCase() + " in " + industry + ", focused on sustainable growth."
        );
    }

    private LinkedInSendResult mockSend(LinkedInMessageRequest request) {
        log.info("[linkedin][MOCK] would send {} to {}", request.type(), request.recipientProviderProfileId());
        return LinkedInSendResult.ok("mock-msg-" + UUID.randomUUID(), "SENT");
    }
}
