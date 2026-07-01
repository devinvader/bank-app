package ru.devinvader.bank.frontui.controller;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import ru.devinvader.bank.frontui.model.AccountPageModel;
import ru.devinvader.bank.frontui.model.CashAction;
import ru.devinvader.bank.frontui.service.AccountFrontService;
import ru.devinvader.bank.frontui.service.CashFrontService;
import ru.devinvader.bank.frontui.service.TransferFrontService;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Slf4j
@Controller
@Validated
@RequiredArgsConstructor
public class MainController {
    private final AccountFrontService accountFrontService;
    private final CashFrontService cashFrontService;
    private final TransferFrontService transferFrontService;

    @GetMapping
    public String index() {
        return "redirect:/account";
    }

    @GetMapping("/account")
    public String getAccount(Model model) {
        fillModel(model, accountFrontService.getAccountPage());
        return "main";
    }

    @PostMapping("/account")
    public String editAccount(Model model,
                               @RequestParam("name") @NotBlank String name,
                               @RequestParam("birthdate") @NotNull @Past LocalDate birthdate) {
        fillModel(model, accountFrontService.updateAccount(name, birthdate));
        return "main";
    }

    @PostMapping("/cash")
    public String editCash(Model model,
                            @RequestParam("value") @Positive BigDecimal value,
                            @RequestParam("action") @NotNull CashAction action) {
        fillModel(model, cashFrontService.processCashOperation(value, action));
        return "main";
    }

    @PostMapping("/transfer")
    public String transfer(Model model,
                            @RequestParam("value") @Positive BigDecimal value,
                            @RequestParam("accountId") @NotNull UUID targetAccountId) {
        fillModel(model, transferFrontService.processTransfer(targetAccountId, value));
        return "main";
    }

    private void fillModel(Model model, AccountPageModel page) {
        model.addAttribute("name", page.name() != null ? page.name() : "");
        model.addAttribute("birthdate", page.birthdate() != null ? page.birthdate().toString() : "");
        model.addAttribute("sum", page.balance() != null ? page.balance() : BigDecimal.ZERO);
        model.addAttribute("accounts", page.transferTargets() != null ? page.transferTargets() : List.of());
        model.addAttribute("errors", page.errors());
        model.addAttribute("info", page.info());
    }
}
