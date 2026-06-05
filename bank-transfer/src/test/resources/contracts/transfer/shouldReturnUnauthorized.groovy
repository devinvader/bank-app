import org.springframework.cloud.contract.spec.Contract

Contract.make {
    description("Запрос без токена")
    request {
        method 'POST'
        url '/api/transfer'
        headers {
            contentType(applicationJson())
        }
        body([
            toLogin: "user2",
            amount: 100
        ])
    }
    response {
        status UNAUTHORIZED()
    }
}
