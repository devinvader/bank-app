package ru.devinvader.bank.frontui.client;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import java.util.function.Supplier;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
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
import ru.devinvader.bank.frontui.mapper.FrontUiMapper;
import ru.devinvader.bank.frontui.model.*;
import ru.devinvader.bank.frontui.service.TokenProvider;

import static ru.devinvader.bank.common.config.RequestIdFilter.SPAN_ID_ATTRIBUTE;
import static ru.devinvader.bank.common.config.RequestIdFilter.TRACE_ID_ATTRIBUTE;

@Slf4j
@Component
public class BankApiClient {

    private final RestClient restClient;
    private final FrontUiMapper frontUiMapper;

    public BankApiClient(TokenProvider tokenProvider,
                         @Value("${gateway.service-id:bank-gateway}") String gatewayServiceId,
                         RestClient.Builder restClientBuilder,
                         FrontUiMapper frontUiMapper) {
        this.frontUiMapper = frontUiMapper;
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

    @CircuitBreaker(name = "accountsGateway", fallbackMethod = "fallbackGetAccount")
    public AccountResponse getAccount() {
        return execute("accounts", () ->
                restClient.get().uri("/api/accounts/me")
                        .retrieve()
                        .onStatus(HttpStatusCode::is4xxClientError, this::onAccounts4xx)
                        .onStatus(HttpStatusCode::is5xxServerError, serverError("Accounts"))
                        .body(AccountResponse.class));
    }

    @CircuitBreaker(name = "accountsGateway", fallbackMethod = "fallbackUpdateAccount")
    public AccountResponse updateAccount(AccountUpdateRequestDto request) {
        return execute("accounts", () ->
                restClient.put().uri("/api/accounts/me")
                        .contentType(MediaType.APPLICATION_JSON).body(request)
                        .retrieve()
                        .onStatus(HttpStatusCode::is4xxClientError, this::onAccountUpdate4xx)
                        .onStatus(HttpStatusCode::is5xxServerError, serverError("Accounts"))
                        .body(AccountResponse.class));
    }

    @CircuitBreaker(name = "cashGateway", fallbackMethod = "fallbackCash")
    public CashResponse deposit(UUID accountId, BigDecimal amount) {
        return executeCashOperation(frontUiMapper.toCashRequest(amount), "deposit");
    }

    @CircuitBreaker(name = "cashGateway", fallbackMethod = "fallbackCash")
    public CashResponse withdraw(UUID accountId, BigDecimal amount) {
        return executeCashOperation(frontUiMapper.toCashRequest(amount), "withdraw");
    }

    private CashResponse executeCashOperation(CashRequestDto request, String operation) {
        return execute("cash", () ->
                restClient.post().uri("/api/cash/{operation}", operation)
                        .contentType(MediaType.APPLICATION_JSON).body(request)
                        .retrieve()
                        .onStatus(status -> status == HttpStatus.BAD_REQUEST, this::onCashBadRequest)
                        .onStatus(HttpStatusCode::is4xxClientError, this::onGeneric4xx)
                        .onStatus(HttpStatusCode::is5xxServerError, serverError("Cash"))
                        .body(CashResponse.class));
    }

    @CircuitBreaker(name = "transferGateway", fallbackMethod = "fallbackTransfer")
    public TransferResponse transfer(TransferRequestDto request) {
        return execute("transfer", () ->
                restClient.post().uri("/api/transfer")
                        .contentType(MediaType.APPLICATION_JSON).body(request)
                        .retrieve()
                        .onStatus(status -> status == HttpStatus.BAD_REQUEST, this::onTransferBadRequest)
                        .onStatus(HttpStatusCode::is4xxClientError, this::onGeneric4xx)
                        .onStatus(HttpStatusCode::is5xxServerError, serverError("Transfer"))
                        .body(TransferResponse.class));
    }

    @CircuitBreaker(name = "accountsGateway", fallbackMethod = "fallbackGetTransferTargets")
    public List<AccountDto> getTransferTargets() {
        return execute("accounts", () -> {
            var result = restClient.get().uri("/api/accounts/transfer-targets")
                    .retrieve()
                    .onStatus(HttpStatusCode::is4xxClientError, this::onGeneric4xx)
                    .onStatus(HttpStatusCode::is5xxServerError, serverError("Accounts"))
                    .body(new ParameterizedTypeReference<List<AccountResponse>>() {});
            if (result == null) return List.of();
            return result.stream()
                    .map(frontUiMapper::toAccountDto)
                    .toList();
        });
    }

    public AccountResponse fallbackGetAccount(Throwable t) {
        log.error("GetAccount fallback: error={}", t.getMessage());
        throw new ServiceUnavailableException("Accounts service unavailable");
    }

    public AccountResponse fallbackUpdateAccount(AccountUpdateRequestDto request, Throwable t) {
        log.error("UpdateAccount fallback: error={}", t.getMessage());
        throw new ServiceUnavailableException("Accounts service unavailable");
    }

    public CashResponse fallbackCash(UUID accountId, BigDecimal amount, Throwable t) {
        log.error("Cash operation fallback: accountId={}, error={}", accountId, t.getMessage());
        throw new ServiceUnavailableException("Cash service unavailable");
    }

    public TransferResponse fallbackTransfer(TransferRequestDto request, Throwable t) {
        log.error("Transfer fallback: error={}", t.getMessage());
        throw new ServiceUnavailableException("Transfer service unavailable");
    }

    public List<AccountDto> fallbackGetTransferTargets(Throwable t) {
        log.error("GetTransferTargets fallback: error={}", t.getMessage());
        throw new ServiceUnavailableException("Accounts service unavailable");
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

    private void onAccounts4xx(HttpRequest req, ClientHttpResponse resp) throws IOException {
        if (resp.getStatusCode() == HttpStatus.UNAUTHORIZED)
            throw new UnauthorizedException("Token expired or invalid");
        throw new BadRequestException("Accounts error: " + resp.getStatusCode());
    }

    private void onAccountUpdate4xx(HttpRequest req, ClientHttpResponse resp) throws IOException {
        if (resp.getStatusCode() == HttpStatus.BAD_REQUEST)
            throw new BadRequestException("Validation error");
        throw new UnauthorizedException("Unauthorized");
    }

    private void onCashBadRequest(HttpRequest req, ClientHttpResponse resp) {
        try {
            var body = new String(resp.getBody().readAllBytes());
            if (body.contains("Insufficient"))
                throw new InsufficientFundsException("Недостаточно средств на счету");
        } catch (IOException ignored) {}
        throw new BadRequestException("Cash operation error");
    }

    private void onTransferBadRequest(HttpRequest req, ClientHttpResponse resp) {
        try {
            var body = new String(resp.getBody().readAllBytes());
            if (body.contains("Insufficient"))
                throw new InsufficientFundsException("Недостаточно средств на счету");
        } catch (IOException ignored) {}
        throw new BadRequestException("Transfer operation error");
    }

    private void onGeneric4xx(HttpRequest req, ClientHttpResponse resp) throws IOException {
        if (resp.getStatusCode() == HttpStatus.UNAUTHORIZED)
            throw new UnauthorizedException("Unauthorized");
        throw new BadRequestException("Client error: " + resp.getStatusCode());
    }

    private static void propagateTraceHeaders(HttpRequest request) {
        var attrs = RequestContextHolder.getRequestAttributes();
        if (attrs != null) {
            var spanId = (String) attrs.getAttribute(SPAN_ID_ATTRIBUTE, RequestAttributes.SCOPE_REQUEST);
            var traceId = (String) attrs.getAttribute(TRACE_ID_ATTRIBUTE, RequestAttributes.SCOPE_REQUEST);
            if (spanId != null) {
                request.getHeaders().set("X-Span-Id", spanId);
            }
            if (traceId != null) {
                request.getHeaders().set("X-Trace-Id", traceId);
            }
        }
    }

    private static ErrorHandler serverError(String service) {
        return (req, resp) -> { throw new ServiceUnavailableException(service + " service unavailable"); };
    }
}
