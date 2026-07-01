import org.springframework.cloud.contract.spec.Contract

Contract.make {
    description("Маршрутизация запроса к cash с авторизацией")
    request {
        method 'POST'
        url '/api/cash/deposit'
        headers {
            contentType(applicationJson())
            header("Authorization", "Bearer test-token")
        }
        body([
            amount: 100
        ])
    }
    response {
        status INTERNAL_SERVER_ERROR()
    }
}
