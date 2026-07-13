import org.springframework.cloud.contract.spec.Contract

Contract.make {
    description("Запрос несуществующего аккаунта по id (сервис-сервис) возвращает 404")
    request {
        method 'GET'
        url '/api/accounts/00000000-0000-0000-0000-000000000000'
        headers {
            header("Authorization", "Bearer test-token")
        }
    }
    response {
        status NOT_FOUND()
        body([
            title: "Account not found",
            status: 404
        ])
    }
}
