import org.springframework.cloud.contract.spec.Contract

Contract.make {
    description("Request to /api/cash/** should be routed when authenticated")
    request {
        method 'POST'
        url '/api/cash/deposit'
        headers {
            contentType(applicationJson())
            header("Authorization", "Bearer test-token")
        }
        body([
            amount: 100
        ])
    }
    response {
        status INTERNAL_SERVER_ERROR()
    }
}
