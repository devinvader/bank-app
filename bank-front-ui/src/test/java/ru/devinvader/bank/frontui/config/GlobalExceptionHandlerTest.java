package ru.devinvader.bank.frontui.config;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.devinvader.bank.frontui.exception.ServiceUnavailableException;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = GlobalExceptionHandlerTest.DummyController.class)
@org.springframework.context.annotation.Import(GlobalExceptionHandler.class)
class GlobalExceptionHandlerTest {

    @Autowired
    private MockMvc mockMvc;

    @RestController
    static class DummyController {
        @GetMapping("/test/error")
        String throwError() {
            throw new ServiceUnavailableException("Test");
        }

        @GetMapping("/test/generic")
        String throwGeneric() {
            throw new RuntimeException("Test generic");
        }
    }

    @Test
    void handleServiceUnavailable_shouldReturnErrorPage() throws Exception {
        mockMvc.perform(get("/test/error"))
                .andExpect(status().isOk())
                .andExpect(view().name("error"))
                .andExpect(model().attributeExists("errors"));
    }

    @Test
    void handleGeneral_shouldReturnErrorPage() throws Exception {
        mockMvc.perform(get("/test/generic"))
                .andExpect(status().isOk())
                .andExpect(view().name("error"))
                .andExpect(model().attributeExists("errors"));
    }
}
