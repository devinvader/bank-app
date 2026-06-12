import org.springframework.cloud.contract.spec.Contract

Contract.make {
    description("Маршрутизация запроса к notifications с авторизацией")
    request {
        method 'POST'
        url '/api/notifications'
        headers {
            contentType(applicationJson())
            header("Authorization", "Bearer test-token")
        }
        body([
            type    : "TRANSFER",
            accountId: "afd94176-3179-4285-9f6b-96fd9131628a",
            amount  : 100,
            message : "Notification test"
        ])
    }
    response {
        status INTERNAL_SERVER_ERROR()
    }
}
