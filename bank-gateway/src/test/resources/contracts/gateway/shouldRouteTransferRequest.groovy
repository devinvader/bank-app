import org.springframework.cloud.contract.spec.Contract

Contract.make {
    description("Маршрутизация запроса к transfer с авторизацией")
    request {
        method 'POST'
        url '/api/transfer'
        headers {
            contentType(applicationJson())
            header("Authorization", "Bearer test-token")
        }
        body([
            toAccountId: "447129a6-bf9b-4dcd-9b35-36d192bb525a",
            amount: 50
        ])
    }
    response {
        status INTERNAL_SERVER_ERROR()
    }
}
