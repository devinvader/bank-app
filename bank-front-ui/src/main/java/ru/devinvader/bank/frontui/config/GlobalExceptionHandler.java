package ru.devinvader.bank.frontui.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.resource.NoResourceFoundException;
import ru.devinvader.bank.frontui.exception.ServiceUnavailableException;

import java.util.List;

@Slf4j
@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ServiceUnavailableException.class)
    public String handleServiceUnavailable(Model model) {
        model.addAttribute("errors", List.of("Сервис временно недоступен. Попробуйте позже."));
        return "error";
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public String handleNotFound(Model model) {
        model.addAttribute("errors", List.of(HttpStatus.NOT_FOUND.getReasonPhrase()));
        return "error";
    }

    @ExceptionHandler(Exception.class)
    public String handleGeneral(Exception ex, Model model) {
        log.error("Unexpected exception", ex);
        model.addAttribute("errors", List.of("Внутренняя ошибка сервера"));
        return "error";
    }
}
