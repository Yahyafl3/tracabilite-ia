package com.pfa.tracabilite_ia.mail;

import java.io.IOException;
import java.util.Map;

/**
 * Thin HTTP transport for Resend API calls (mockable in tests).
 */
@FunctionalInterface
public interface ResendHttpPoster {

    record HttpResult(int statusCode, String body) {
    }

    HttpResult post(String url, Map<String, String> headers, String jsonBody)
            throws IOException, InterruptedException;
}
