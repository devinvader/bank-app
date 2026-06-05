import org.springframework.cloud.contract.spec.Contract

Contract.make {
    description("Списание средств со счёта")
    request {
        method 'POST'
        url '/api/accounts/user1/debit'
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
