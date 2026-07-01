import org.springframework.cloud.contract.spec.Contract

Contract.make {
    description("Запрос без токена")
    request {
        method 'POST'
        url '/api/notifications'
        headers {
            contentType(applicationJson())
        }
    }
    response {
        status UNAUTHORIZED()
    }
}
