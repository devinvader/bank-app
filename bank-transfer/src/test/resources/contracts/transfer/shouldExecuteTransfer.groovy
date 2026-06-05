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
            toLogin: "user2",
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
            fromLogin: "user1",
            toLogin: "user2",
            amount: 100,
            status: $(anyNonBlankString()),
            timestamp: $(anyNonBlankString())
        ])
    }
}
