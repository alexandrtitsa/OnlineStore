package com.onlinestore.web.exception;

import java.time.Instant;
import java.util.Map;

public record ValidationErrorResponse(
        int status,
        String error,
        Map<String, String> validationErrors,
        Instant timestamp
) {}