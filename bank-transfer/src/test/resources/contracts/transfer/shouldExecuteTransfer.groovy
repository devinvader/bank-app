import org.springframework.cloud.contract.spec.Contract

Contract.make {
    description("Выполнение перевода")
    request {
        method 'POST'
        url '/api/transfer'
        headers {
            contentType(applicationJson())
            header("Authorization", "Bearer test-token")
        }
        body([
            toAccountId: "447129a6-bf9b-4dcd-9b35-36d192bb525a",
            amount: 100
        ])
    }
    response {
        status OK()
        headers {
            contentType(applicationJson())
        }
        body([
            id: $(anyUuid()),
            fromAccountId: "afd94176-3179-4285-9f6b-96fd9131628a",
            toAccountId: "447129a6-bf9b-4dcd-9b35-36d192bb525a",
            amount: 100,
            status: $(anyNonBlankString()),
            timestamp: $(anyNonBlankString())
        ])
    }
}
