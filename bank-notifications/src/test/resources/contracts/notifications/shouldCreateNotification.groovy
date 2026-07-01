import org.springframework.cloud.contract.spec.Contract

Contract.make {
    description("Создание уведомления")
    request {
        method 'POST'
        url '/api/notifications'
        headers {
            contentType(applicationJson())
            header("Authorization", "Bearer test-token")
        }
        body([
            type: "TRANSFER",
            accountId: "afd94176-3179-4285-9f6b-96fd9131628a",
            amount: 100.50,
            message: "Получено от user2"
        ])
    }
    response {
        status OK()
        headers {
            contentType(applicationJson())
        }
        body([
            id: $(anyUuid()),
            type: "TRANSFER",
            accountId: "afd94176-3179-4285-9f6b-96fd9131628a",
            amount: 100.50,
            message: "Получено от user2",
            status: "SENT",
            createdAt: $(anyNonBlankString()),
            sentAt: $(anyNonBlankString())
        ])
        bodyMatchers {
            jsonPath('$.status', byRegex('(SENT|FAILED)'))
        }
    }
}
