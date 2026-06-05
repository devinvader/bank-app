import org.springframework.cloud.contract.spec.Contract

Contract.make {
    description("Обновление данных аккаунта")
    request {
        method 'PUT'
        url '/api/accounts/me'
        headers {
            contentType(applicationJson())
            header("Authorization", "Bearer test-token")
        }
        body([
            name: "Новое Имя",
            birthdate: "1990-01-01"
        ])
    }
    response {
        status OK()
        headers {
            contentType(applicationJson())
        }
        body([
            login: "user1",
            name: "Новое Имя",
            birthdate: "1990-01-01",
            balance: $(anyNumber())
        ])
    }
}
