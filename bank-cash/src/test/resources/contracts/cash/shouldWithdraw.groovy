import org.springframework.cloud.contract.spec.Contract

Contract.make {
    description("Снятие со счета")
    request {
        method 'POST'
        url '/api/cash/withdraw'
        headers {
            contentType(applicationJson())
            header("Authorization", "Bearer test-token")
        }
        body([
            amount: 50
        ])
    }
    response {
        status OK()
        headers {
            contentType(applicationJson())
        }
        body([
            accountId: "user1",
            newBalance: $(anyPositiveInt()),
            type: "WITHDRAWAL",
            amount: 50
        ])
    }
}
