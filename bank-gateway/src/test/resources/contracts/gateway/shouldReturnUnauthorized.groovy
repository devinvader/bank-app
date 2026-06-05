import org.springframework.cloud.contract.spec.Contract

Contract.make {
    description("API endpoints should return 401 without JWT token")
    request {
        method 'GET'
        url '/api/accounts/me'
    }
    response {
        status UNAUTHORIZED()
    }
}
