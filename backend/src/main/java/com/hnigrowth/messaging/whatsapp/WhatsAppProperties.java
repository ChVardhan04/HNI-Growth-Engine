package com.hnigrowth.messaging.whatsapp;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/** WhatsApp Business Cloud API (Meta) config. TODO(integration): provision a
 * WhatsApp Business Account + app at developers.facebook.com/docs/whatsapp,
 * then set WHATSAPP_PHONE_NUMBER_ID and WHATSAPP_ACCESS_TOKEN. Until then,
 * mock-mode logs sends instead of dispatching. */
@Component
@ConfigurationProperties(prefix = "app.whatsapp")
public class WhatsAppProperties {
    private String baseUrl = "https://graph.facebook.com/v20.0";
    private String phoneNumberId = "";
    private String accessToken = "";
    private boolean mockMode = true;

    public String getBaseUrl() { return baseUrl; }
    public void setBaseUrl(String baseUrl) { this.baseUrl = baseUrl; }
    public String getPhoneNumberId() { return phoneNumberId; }
    public void setPhoneNumberId(String phoneNumberId) { this.phoneNumberId = phoneNumberId; }
    public String getAccessToken() { return accessToken; }
    public void setAccessToken(String accessToken) { this.accessToken = accessToken; }
    public boolean isMockMode() { return mockMode; }
    public void setMockMode(boolean mockMode) { this.mockMode = mockMode; }

    public boolean isConfigured() {
        return phoneNumberId != null && !phoneNumberId.isBlank()
                && accessToken != null && !accessToken.isBlank();
    }
}
