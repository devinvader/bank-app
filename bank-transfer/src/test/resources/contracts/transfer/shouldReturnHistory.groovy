import org.springframework.cloud.contract.spec.Contract

Contract.make {
    description("Получение истории переводов")
    request {
        method 'GET'
        url '/api/transfer/history'
        headers {
            header("Authorization", "Bearer test-token")
        }
    }
    response {
        status OK()
        headers {
            contentType(applicationJson())
        }
    }
}
