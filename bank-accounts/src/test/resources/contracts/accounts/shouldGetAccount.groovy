import org.springframework.cloud.contract.spec.Contract

Contract.make {
    description("Получение аккаунта текущего пользователя")
    request {
        method 'GET'
        url '/api/accounts/me'
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
