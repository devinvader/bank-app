import org.springframework.cloud.contract.spec.Contract

Contract.make {
    description("Маршрутизация запроса к accounts с авторизацией")
    request {
        method 'GET'
        url '/api/accounts/me'
        headers {
            header("Authorization", "Bearer test-token")
        }
    }
    response {
        status INTERNAL_SERVER_ERROR()
    }
}
