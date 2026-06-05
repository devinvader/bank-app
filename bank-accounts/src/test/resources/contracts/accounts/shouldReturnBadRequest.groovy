import org.springframework.cloud.contract.spec.Contract

Contract.make {
    description("Невалидный запрос на обновление")
    request {
        method 'PUT'
        url '/api/accounts/me'
        headers {
            contentType(applicationJson())
            header("Authorization", "Bearer test-token")
        }
        body([
            name: "",
            birthdate: null
        ])
    }
    response {
        status BAD_REQUEST()
    }
}
