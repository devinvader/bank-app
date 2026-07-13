import org.springframework.cloud.contract.spec.Contract

Contract.make {
    description("Проверка существования аккаунтов (сервис-сервис)")
    request {
        method 'POST'
        url '/api/accounts/exists'
        headers {
            contentType(applicationJson())
            header("Authorization", "Bearer test-token")
        }
        body([
            accountIds: ["afd94176-3179-4285-9f6b-96fd9131628a", "00000000-0000-0000-0000-000000000000"]
        ])
    }
    response {
        status OK()
        headers {
            contentType(applicationJson())
        }
        body([
            missing: ["00000000-0000-0000-0000-000000000000"]
        ])
    }
}
