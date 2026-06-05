import org.springframework.cloud.contract.spec.Contract

Contract.make {
    description("Запрос без токена")
    request {
        method 'POST'
        url '/api/cash/deposit'
        headers {
            contentType(applicationJson())
        }
        body([
            amount: 100
        ])
    }
    response {
        status UNAUTHORIZED()
    }
}
