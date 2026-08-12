package com.hnigrowth.linkedin.exceptions;

/** Raised whenever the LinkedIn/Unipile provider call fails or is unreachable. */
public class LinkedInApiException extends RuntimeException {
    public LinkedInApiException(String message) { super(message); }
    public LinkedInApiException(String message, Throwable cause) { super(message, cause); }
}
