import org.springframework.cloud.contract.spec.Contract

Contract.make {
    description("Request to /api/accounts/** should be routed when authenticated")
    request {
        method 'GET'
        url '/api/accounts/me'
        headers {
            header("Authorization", "Bearer test-token")
        }
    }
    response {
        status INTERNAL_SERVER_ERROR()
    }
}
