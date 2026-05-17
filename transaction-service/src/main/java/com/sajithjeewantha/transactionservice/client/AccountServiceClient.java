package com.sajithjeewantha.transactionservice.client;

import com.sajithjeewantha.transactionservice.dto.TransactionDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class AccountServiceClient {

    private final RestTemplate restTemplate;

    @Value("${account.service.url}")
    private String accountServiceUrl;

    @Value("${internal.service.secret}")
    private String internalSecret;

    // ─── Get Account by ID ────────────────────────────────────
    public TransactionDTO.AccountInfo getAccount(Long accountId) {
        String url = accountServiceUrl + "/internal/accounts/" + accountId;
        log.info("Calling account service: GET {}", url);

        HttpHeaders headers = buildInternalHeaders();
        HttpEntity<Void> entity = new HttpEntity<>(headers);

        ResponseEntity<TransactionDTO.AccountInfo> response = restTemplate.exchange(
                url, HttpMethod.GET, entity, TransactionDTO.AccountInfo.class);

        return response.getBody();
    }

    // ─── Debit Account ────────────────────────────────────────
    public void debitAccount(Long accountId, BigDecimal amount) {
        String url = accountServiceUrl + "/internal/accounts/" + accountId + "/balance";
        log.info("Debiting {} from account {}", amount, accountId);

        Map<String, Object> body = Map.of(
                "amount", amount,
                "operation", "DEBIT"
        );

        HttpHeaders headers = buildInternalHeaders();
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);
        restTemplate.exchange(url, HttpMethod.PUT, entity, Object.class);
    }

    // ─── Credit Account ───────────────────────────────────────
    public void creditAccount(Long accountId, BigDecimal amount) {
        String url = accountServiceUrl + "/internal/accounts/" + accountId + "/balance";
        log.info("Crediting {} to account {}", amount, accountId);

        Map<String, Object> body = Map.of(
                "amount", amount,
                "operation", "CREDIT"
        );

        HttpHeaders headers = buildInternalHeaders();
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);
        restTemplate.exchange(url, HttpMethod.PUT, entity, Object.class);
    }

    private HttpHeaders buildInternalHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("X-Internal-Secret", internalSecret);
        return headers;
    }
}
