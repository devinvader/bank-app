import org.springframework.cloud.contract.spec.Contract

Contract.make {
    description("Request to /api/notifications should be routed when authenticated")
    request {
        method 'POST'
        url '/api/notifications'
        headers {
            contentType(applicationJson())
            header("Authorization", "Bearer test-token")
        }
        body([
            type    : "TRANSFER",
            accountId: "user1",
            amount  : 100,
            message : "Notification test"
        ])
    }
    response {
        status INTERNAL_SERVER_ERROR()
    }
}
