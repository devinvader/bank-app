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
            login: "user1",
            name: $(anyNonBlankString()),
            birthdate: $(anyNonBlankString()),
            balance: $(anyNumber())
        ])
    }
}
