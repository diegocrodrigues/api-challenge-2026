package com.cotefacil.gateway.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClient;

@Tag(name = "Proxy", description = "Roteamento autenticado para a API de Pedidos")
@RestController
@RequestMapping("/api")
public class GatewayController {

    private static final Logger log = LoggerFactory.getLogger(GatewayController.class);

    private final WebClient webClient;

    public GatewayController(WebClient webClient) {
        this.webClient = webClient;
    }

    @Operation(
            summary = "Proxy para a API de Pedidos",
            description = "Repassa a requisição autenticada para a API Orders e retorna a resposta sem modificação"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Resposta da API Orders"),
            @ApiResponse(responseCode = "401", description = "Token JWT ausente ou inválido"),
            @ApiResponse(responseCode = "503", description = "API Orders indisponível")
    })
    @RequestMapping(
            value = "/orders/**",
            method = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE}
    )
    public ResponseEntity<String> proxy(HttpServletRequest request,
                                        @RequestBody(required = false) String body) {
        String uri = buildUri(request);
        String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
        HttpMethod method = HttpMethod.valueOf(request.getMethod());

        log.debug("Proxy {} {} → Orders API", method, uri);

        WebClient.RequestBodySpec spec = webClient
                .method(method)
                .uri(uri)
                .contentType(MediaType.APPLICATION_JSON);

        if (authHeader != null) {
            spec = spec.header(HttpHeaders.AUTHORIZATION, authHeader);
        }

        WebClient.RequestHeadersSpec<?> headersSpec = hasBody(body) ? spec.bodyValue(body) : spec;

        return headersSpec
                .exchangeToMono(response -> response.toEntity(String.class))
                .block();
    }

    private String buildUri(HttpServletRequest request) {
        String query = request.getQueryString();
        return query != null
                ? request.getRequestURI() + "?" + query
                : request.getRequestURI();
    }

    private boolean hasBody(String body) {
        return body != null && !body.isBlank();
    }
}
