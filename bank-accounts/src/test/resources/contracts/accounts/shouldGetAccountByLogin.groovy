import org.springframework.cloud.contract.spec.Contract

Contract.make {
    description("Получение аккаунта по логину (сервис-сервис)")
    request {
        method 'GET'
        url '/api/accounts/afd94176-3179-4285-9f6b-96fd9131628a'
        headers {
            header("Authorization", "Bearer test-token")
        }
    }
    response {
        status OK()
        headers {
            contentType(applicationJson())
        }
        body([
            accountId: "afd94176-3179-4285-9f6b-96fd9131628a",
            name: $(anyNonBlankString()),
            birthdate: $(anyNonBlankString()),
            balance: $(anyNumber())
        ])
    }
}
