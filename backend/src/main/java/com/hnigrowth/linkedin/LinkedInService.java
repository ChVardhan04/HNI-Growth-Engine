package com.hnigrowth.linkedin;

import com.hnigrowth.linkedin.dto.ConnectionStatus;
import com.hnigrowth.linkedin.dto.LinkedInMessageRequest;
import com.hnigrowth.linkedin.dto.LinkedInProfileDto;
import com.hnigrowth.linkedin.dto.LinkedInSearchRequest;
import com.hnigrowth.linkedin.dto.LinkedInSendResult;
import com.hnigrowth.provider.LinkedInProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Business-facing LinkedIn service. Controllers depend on this (or on
 * LinkedInProvider directly), never on LinkedInApiClient.
 */
@Service
@RequiredArgsConstructor
public class LinkedInService implements LinkedInProvider {

    private final LinkedInApiClient apiClient;

    @Override
    public List<LinkedInProfileDto> search(LinkedInSearchRequest request) {
        return apiClient.search(request);
    }

    @Override
    public LinkedInSendResult send(LinkedInMessageRequest request) {
        return apiClient.sendMessage(request);
    }

    @Override
    public ConnectionStatus checkConnectionStatus(String personUrl) {
        return apiClient.checkConnectionStatus(personUrl);
    }
}
