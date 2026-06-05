import org.springframework.cloud.contract.spec.Contract

Contract.make {
    description("Получение аккаунта по логину (сервис-сервис)")
    request {
        method 'GET'
        url '/api/accounts/user1'
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
