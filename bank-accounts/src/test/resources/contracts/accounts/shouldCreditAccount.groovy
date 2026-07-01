import org.springframework.cloud.contract.spec.Contract

Contract.make {
    description("Зачисление средств на счёт")
    request {
        method 'POST'
        url '/api/accounts/afd94176-3179-4285-9f6b-96fd9131628a/credit'
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
