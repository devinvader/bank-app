package ru.devinvader.bank.frontui.client;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import java.util.function.Supplier;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClient.ResponseSpec.ErrorHandler;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import ru.devinvader.bank.frontui.exception.*;
import ru.devinvader.bank.frontui.model.*;
import ru.devinvader.bank.frontui.service.TokenProvider;

import static ru.devinvader.bank.frontui.config.RequestIdFilter.REQUEST_ID_ATTRIBUTE;

@Slf4j
@Component
public class BankApiClient {

    private final RestClient restClient;

    public BankApiClient(TokenProvider tokenProvider,
                         @Value("${gateway.service-id:bank-gateway}") String gatewayServiceId,
                         RestClient.Builder restClientBuilder) {
        this.restClient = restClientBuilder
                .baseUrl("http://" + gatewayServiceId)
                .defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                .requestInterceptor((request, body, execution) -> {
                    request.getHeaders().setBearerAuth(tokenProvider.getAccessToken());
                    propagateTraceHeaders(request);
                    return execution.execute(request, body);
                })
                .build();
    }

    public AccountResponse getAccount() {
        return execute("accounts", () ->
                restClient.get().uri("/api/accounts/me")
                        .retrieve()
                        .onStatus(HttpStatusCode::is4xxClientError, (req, resp) -> onAccounts4xx(req, resp))
                        .onStatus(HttpStatusCode::is5xxServerError, serverError("Accounts"))
                        .body(AccountResponse.class));
    }

    public AccountResponse updateAccount(AccountUpdateRequestDto request) {
        return execute("accounts", () ->
                restClient.put().uri("/api/accounts/me")
                        .contentType(MediaType.APPLICATION_JSON).body(request)
                        .retrieve()
                        .onStatus(HttpStatusCode::is4xxClientError, (req, resp) -> onAccountUpdate4xx(req, resp))
                        .onStatus(HttpStatusCode::is5xxServerError, serverError("Accounts"))
                        .body(AccountResponse.class));
    }

    public CashResponse deposit(String accountId, BigDecimal amount) {
        return executeCashOperation(new CashRequestDto(accountId, amount), "deposit");
    }

    public CashResponse withdraw(String accountId, BigDecimal amount) {
        return executeCashOperation(new CashRequestDto(accountId, amount), "withdraw");
    }

    private CashResponse executeCashOperation(CashRequestDto request, String operation) {
        return execute("cash", () ->
                restClient.post().uri("/api/cash/{operation}", operation)
                        .contentType(MediaType.APPLICATION_JSON).body(request)
                        .retrieve()
                        .onStatus(status -> status == HttpStatus.BAD_REQUEST, (req, resp) -> onCashBadRequest(req, resp))
                        .onStatus(HttpStatusCode::is4xxClientError, (req, resp) -> onGeneric4xx(req, resp))
                        .onStatus(HttpStatusCode::is5xxServerError, serverError("Cash"))
                        .body(CashResponse.class));
    }

    public TransferResponse transfer(TransferRequestDto request) {
        return execute("transfer", () ->
                restClient.post().uri("/api/transfer")
                        .contentType(MediaType.APPLICATION_JSON).body(request)
                        .retrieve()
                        .onStatus(status -> status == HttpStatus.BAD_REQUEST, (req, resp) -> onGeneric4xx(req, resp))
                        .onStatus(HttpStatusCode::is4xxClientError, (req, resp) -> onGeneric4xx(req, resp))
                        .onStatus(HttpStatusCode::is5xxServerError, serverError("Transfer"))
                        .body(TransferResponse.class));
    }

    public List<AccountDto> getTransferTargets() {
        return execute("accounts", () -> {
            var result = restClient.get().uri("/api/accounts/transfer-targets")
                    .retrieve()
                    .onStatus(HttpStatusCode::is4xxClientError, (req, resp) -> onGeneric4xx(req, resp))
                    .onStatus(HttpStatusCode::is5xxServerError, serverError("Accounts"))
                    .body(new ParameterizedTypeReference<List<AccountResponse>>() {});
            if (result == null) return List.of();
            return result.stream()
                    .map(r -> new AccountDto(r.login(), r.name()))
                    .toList();
        });
    }

    private <T> T execute(String serviceName, Supplier<T> block) {
        try {
            return block.get();
        } catch (BankApiException e) { throw e; }
        catch (Exception e) {
            log.error("Failed to call {}: {}", serviceName, e.getMessage());
            throw new ServiceUnavailableException("Cannot reach " + serviceName + " service", e);
        }
    }

    private void onAccounts4xx(HttpRequest req, ClientHttpResponse resp) throws java.io.IOException {
        if (resp.getStatusCode() == HttpStatus.UNAUTHORIZED)
            throw new UnauthorizedException("Token expired or invalid");
        throw new BadRequestException("Accounts error: " + resp.getStatusCode());
    }

    private void onAccountUpdate4xx(HttpRequest req, ClientHttpResponse resp) throws java.io.IOException {
        if (resp.getStatusCode() == HttpStatus.BAD_REQUEST)
            throw new BadRequestException("Validation error");
        throw new UnauthorizedException("Unauthorized");
    }

    private void onCashBadRequest(HttpRequest req, ClientHttpResponse resp) throws java.io.IOException {
        try {
            var body = new String(resp.getBody().readAllBytes());
            if (body.contains("Insufficient"))
                throw new InsufficientFundsException("Недостаточно средств на счету");
        } catch (java.io.IOException ignored) { /* fall through */ }
        throw new BadRequestException("Cash operation error");
    }

    private void onGeneric4xx(HttpRequest req, ClientHttpResponse resp) throws java.io.IOException {
        if (resp.getStatusCode() == HttpStatus.UNAUTHORIZED)
            throw new UnauthorizedException("Unauthorized");
        throw new BadRequestException("Client error: " + resp.getStatusCode());
    }

    private static void propagateTraceHeaders(HttpRequest request) {
        var attrs = RequestContextHolder.getRequestAttributes();
        if (attrs != null) {
            var requestId = (String) attrs.getAttribute(REQUEST_ID_ATTRIBUTE, RequestAttributes.SCOPE_REQUEST);
            if (requestId != null) {
                request.getHeaders().set("X-Request-Id", requestId);
                request.getHeaders().set("X-Trace-Id", UUID.randomUUID().toString());
            }
        }
    }

    private static ErrorHandler serverError(String service) {
        return (req, resp) -> { throw new ServiceUnavailableException(service + " service unavailable"); };
    }
}
