import org.springframework.cloud.contract.spec.Contract

Contract.make {
    description("Невалидный запрос на cash операцию")
    request {
        method 'POST'
        url '/api/cash/deposit'
        headers {
            contentType(applicationJson())
            header("Authorization", "Bearer test-token")
        }
        body([
            amount: -1
        ])
    }
    response {
        status BAD_REQUEST()
    }
}
