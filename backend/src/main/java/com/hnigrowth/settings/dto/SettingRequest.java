package com.hnigrowth.settings.dto;

public record SettingRequest(String key, String value, boolean secret, String description) {}
