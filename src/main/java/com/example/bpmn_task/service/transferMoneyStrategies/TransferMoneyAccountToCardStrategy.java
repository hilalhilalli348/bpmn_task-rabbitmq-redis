package com.example.bpmn_task.service.transferMoneyStrategies;

import com.example.bpmn_task.dto.response.AccountCardMoneyContainer;
import com.example.bpmn_task.dto.response.TransferResponse;
import com.example.bpmn_task.entity.Account;
import com.example.bpmn_task.entity.Card;
import com.example.bpmn_task.entity.Transfer;
import com.example.bpmn_task.enums.*;
import com.example.bpmn_task.mapper.TransferMapper;
import com.example.bpmn_task.repository.AccountRepository;
import com.example.bpmn_task.repository.CardRepository;
import com.example.bpmn_task.repository.TransferRepository;
import com.example.bpmn_task.utils.CurrencyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.MathContext;


@Component
public class TransferMoneyAccountToCardStrategy implements TransferMoneyStrategy {
    private final AccountRepository accountRepository;
    private final CardRepository cardRepository;
    private final TransferRepository transferRepository;

    private final CurrencyService currencyService;

    @Autowired
    public TransferMoneyAccountToCardStrategy(AccountRepository accountRepository, CardRepository cardRepository, TransferRepository transferRepository, CurrencyService currencyService) {
        this.accountRepository = accountRepository;
        this.cardRepository = cardRepository;
        this.transferRepository = transferRepository;
        this.currencyService = currencyService;
    }

    @Transactional
    @Override
    public TransferResponse transfer(AccountCardMoneyContainer accountCardMoneyContainer) {

        Account creditorAccount = accountRepository.findByAccountNumber(accountCardMoneyContainer.getCreditorAccount().getAccountNumber()).get();
        Card debitorCard = cardRepository.findByCardNumber(accountCardMoneyContainer.getDebitorCard().getCardNumber()).get();
        BigDecimal outgoingMoney = accountCardMoneyContainer.getOutgoingMoney();


        if (creditorAccount.getCurrency() == debitorCard.getCurrency()) {

            creditorAccount.setBalance(creditorAccount.getBalance().subtract(outgoingMoney));
            debitorCard.setBalance(debitorCard.getBalance().add(outgoingMoney));

            saveAllChanging(creditorAccount, debitorCard);

        } else {

            if (creditorAccount.getCurrency() == CurrencyEnum.AZN) {

                if (debitorCard.getCurrency() == CurrencyEnum.USD) {

                    BigDecimal currentCurrency = currencyService.getCurrency("USD");
                    BigDecimal outgoingMoneyAZN = outgoingMoney; //azn

                    BigDecimal outgoingMoneyUSD = outgoingMoney
                            .divide(currentCurrency, MathContext.DECIMAL32);

                    creditorAccount.setBalance(creditorAccount.getBalance().subtract(outgoingMoneyAZN));
                    debitorCard.setBalance(debitorCard.getBalance().add(outgoingMoneyUSD));

                    saveAllChanging(creditorAccount, debitorCard);
                }

            } else if (creditorAccount.getCurrency() == CurrencyEnum.USD) {

                if (debitorCard.getCurrency() == CurrencyEnum.AZN) {

                    BigDecimal currentCurrency = currencyService.getCurrency("USD");
                    BigDecimal outgoingMoneyUSD = outgoingMoney; //azn

                    BigDecimal outgoingMoneyAZN = outgoingMoney
                            .multiply(currentCurrency, MathContext.DECIMAL32);


                    creditorAccount.setBalance(creditorAccount.getBalance().subtract(outgoingMoneyUSD));
                    debitorCard.setBalance(debitorCard.getBalance().add(outgoingMoneyAZN));

                    saveAllChanging(creditorAccount, debitorCard);
                }

            }

        }


        return convertToTransferResponse(creditorAccount, debitorCard);
    }

    private void saveAllChanging(Account creditorAccount, Card debitorCard) {

        creditorAccount
                .getCardList()
                .forEach(
                        cardOfAccount ->
                                cardOfAccount.setBalance(creditorAccount.getBalance())
                );

        accountRepository.save(creditorAccount);


        Account accountOfDebitorCard = debitorCard.getAccount();
        accountOfDebitorCard.setBalance(debitorCard.getBalance());

        accountOfDebitorCard
                .getCardList()
                .forEach(
                        cardOfAccount ->
                                cardOfAccount.setBalance(debitorCard.getBalance())
                );


        accountRepository.save(accountOfDebitorCard);

    }


    private TransferResponse convertToTransferResponse(Account creditorAccount, Card debitorCard) {

        TransferResponse transferResponse = new TransferResponse();


        transferResponse.setTransferType(TransferTypeEnum.ACCOUNT_TO_CARD)
                .setCreditorAccountID(creditorAccount.getId())
                .setCreditorCurrency(creditorAccount.getCurrency())
                .setCreditorAmount(creditorAccount.getBalance())


                .setDebitorCardID(debitorCard.getId())
                .setDebitorCurrency(debitorCard.getCurrency())
                .setDebitorAmount(debitorCard.getBalance())

                .setStatus(TransferStatusEnum.SUCCESS);


        Transfer transferm = TransferMapper.MAPPER.toTransfer(transferResponse);

        Transfer savedTransfer = transferRepository.save(transferm);
        transferResponse.setId(savedTransfer.getId());

        return transferResponse;
    }


}