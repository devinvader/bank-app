package ru.devinvader.bank.frontui.service;

import ru.devinvader.bank.frontui.model.AccountPageModel;
import java.time.LocalDate;

public interface AccountFrontService {
    AccountPageModel getAccountPage();
    AccountPageModel updateAccount(String name, LocalDate birthdate);
}
