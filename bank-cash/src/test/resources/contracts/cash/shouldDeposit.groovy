import org.springframework.cloud.contract.spec.Contract

Contract.make {
    description("Пополнение счета")
    request {
        method 'POST'
        url '/api/cash/deposit'
        headers {
            contentType(applicationJson())
            header("Authorization", "Bearer test-token")
        }
        body([
            amount: 100
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
            type: "DEPOSIT",
            amount: 100
        ])
    }
}
