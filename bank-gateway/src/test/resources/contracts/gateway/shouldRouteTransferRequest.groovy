import org.springframework.cloud.contract.spec.Contract

Contract.make {
    description("Request to /api/transfer should be routed when authenticated")
    request {
        method 'POST'
        url '/api/transfer'
        headers {
            contentType(applicationJson())
            header("Authorization", "Bearer test-token")
        }
        body([
            toLogin: "user2",
            amount: 50
        ])
    }
    response {
        status INTERNAL_SERVER_ERROR()
    }
}
