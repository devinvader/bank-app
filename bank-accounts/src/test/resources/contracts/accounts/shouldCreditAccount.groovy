import org.springframework.cloud.contract.spec.Contract

Contract.make {
    description("Зачисление средств на счёт")
    request {
        method 'POST'
        url '/api/accounts/user1/credit'
        headers {
            contentType(applicationJson())
            header("Authorization", "Bearer test-token")
        }
        body([amount: 100])
    }
    response {
        status OK()
    }
}
