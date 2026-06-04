import org.springframework.cloud.contract.spec.Contract

Contract.make {
    description("Запрос некорректной информации")
    request {
        method 'POST'
        url '/api/notifications'
        headers {
            contentType(applicationJson())
            header("Authorization", "Bearer test-token")
        }
        body([
            type: "",
            accountId: "",
            amount: -1,
            message: ""
        ])
    }
    response {
        status BAD_REQUEST()
    }
}
