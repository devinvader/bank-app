import org.springframework.cloud.contract.spec.Contract

Contract.make {
    description("Невалидный запрос на перевод")
    request {
        method 'POST'
        url '/api/transfer'
        headers {
            contentType(applicationJson())
            header("Authorization", "Bearer test-token")
        }
        body([
            toLogin: "",
            amount: -1
        ])
    }
    response {
        status BAD_REQUEST()
    }
}
