import org.springframework.cloud.contract.spec.Contract

Contract.make {
    description("Запрос без токена")
    request {
        method 'GET'
        url '/api/accounts/me'
        headers {
            header("Authorization", absent())
        }
    }
    response {
        status UNAUTHORIZED()
    }
}
