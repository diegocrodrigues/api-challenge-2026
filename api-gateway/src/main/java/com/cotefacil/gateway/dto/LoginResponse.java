package com.cotefacil.gateway.dto;

public record LoginResponse(
        String token,
        String type,
        long expiresIn
) {}
