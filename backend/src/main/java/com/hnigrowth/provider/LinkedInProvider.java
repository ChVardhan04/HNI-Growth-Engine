package com.hnigrowth.provider;

import com.hnigrowth.linkedin.dto.ConnectionStatus;
import com.hnigrowth.linkedin.dto.LinkedInMessageRequest;
import com.hnigrowth.linkedin.dto.LinkedInProfileDto;
import com.hnigrowth.linkedin.dto.LinkedInSearchRequest;
import com.hnigrowth.linkedin.dto.LinkedInSendResult;

import java.util.List;

/**
 * Abstraction over "the current lead-source social network". Today this is
 * LinkedIn via Unipile; tomorrow it could be Apollo or Clay with minimal
 * ripple, since every caller depends on this interface, not a concrete client.
 */
public interface LinkedInProvider {
    List<LinkedInProfileDto> search(LinkedInSearchRequest request);
    LinkedInSendResult send(LinkedInMessageRequest request);
    ConnectionStatus checkConnectionStatus(String personUrl);
}
