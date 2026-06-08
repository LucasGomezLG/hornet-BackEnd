package com.hornetimports.vendedor.dto;

public record FirmaResponse(
        String signature,
        long timestamp,
        String folder,
        String apiKey,
        String cloudName
) {}
