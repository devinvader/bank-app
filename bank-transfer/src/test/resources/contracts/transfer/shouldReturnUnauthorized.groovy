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
            toAccountId: "447129a6-bf9b-4dcd-9b35-36d192bb525a",
            amount: 100
        ])
    }
    response {
        status UNAUTHORIZED()
    }
}
