package com.hnigrowth.linkedin.dto;

/** Result of st.checkConnectionStatus. Only CONNECTED profiles can receive a
 * direct LinkedIn message -- LinkedIn does not allow messaging a stranger
 * without InMail (which Linked API/standard actions don't cover). */
public enum ConnectionStatus { CONNECTED, PENDING, NOT_CONNECTED, UNKNOWN }
