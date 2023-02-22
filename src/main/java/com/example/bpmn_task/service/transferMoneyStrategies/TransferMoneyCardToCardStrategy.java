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
public class TransferMoneyCardToCardStrategy implements TransferMoneyStrategy {

    private final CardRepository cardRepository;
    private final AccountRepository accountRepository;
    private final TransferRepository transferRepository;
    private final CurrencyService currencyService;

    @Autowired
    public TransferMoneyCardToCardStrategy(CardRepository cardRepository, AccountRepository accountRepository, TransferRepository transferRepository, CurrencyService currencyService) {
        this.cardRepository = cardRepository;
        this.accountRepository = accountRepository;
        this.transferRepository = transferRepository;
        this.currencyService = currencyService;
    }


    @Transactional
    @Override
    public TransferResponse transfer(AccountCardMoneyContainer accountCardMoneyContainer) {

        Card creditorCard = cardRepository.findByCardNumber(accountCardMoneyContainer.getCreditorCard().getCardNumber()).get();
        Card debitorCard = cardRepository.findByCardNumber(accountCardMoneyContainer.getDebitorCard().getCardNumber()).get();
        BigDecimal outgoingMoney = accountCardMoneyContainer.getOutgoingMoney();


        if (creditorCard.getCurrency() == debitorCard.getCurrency()) {


            creditorCard.setBalance(creditorCard.getBalance().subtract(outgoingMoney));
            debitorCard.setBalance(debitorCard.getBalance().add(outgoingMoney));

            saveAllForCard(creditorCard, debitorCard);


        } else if (creditorCard.getCurrency() != debitorCard.getCurrency()) {

            if (creditorCard.getCurrency() == CurrencyEnum.AZN) {

                if (debitorCard.getCurrency() == CurrencyEnum.USD) {
                    BigDecimal currentCurrency = currencyService.getCurrency("USD");
                    BigDecimal outgoingMoneyAZN = outgoingMoney; //azn

                    BigDecimal outgoingMoneyUSD = outgoingMoney
                            .divide(currentCurrency, MathContext.DECIMAL32);


                    creditorCard.setBalance(creditorCard.getBalance().subtract(outgoingMoneyAZN));
                    debitorCard.setBalance(debitorCard.getBalance().add(outgoingMoneyUSD));

                    saveAllForCard(creditorCard, debitorCard);

                }

            } else if (creditorCard.getCurrency() == CurrencyEnum.USD) {

                if (debitorCard.getCurrency() == CurrencyEnum.AZN) {

                    BigDecimal currentCurrency = currencyService.getCurrency("USD");
                    BigDecimal outgoingMoneyUSD = outgoingMoney; //azn

                    BigDecimal outgoingMoneyAZN = outgoingMoney
                            .multiply(currentCurrency, MathContext.DECIMAL32);


                    creditorCard.setBalance(creditorCard.getBalance().subtract(outgoingMoneyUSD));
                    debitorCard.setBalance(debitorCard.getBalance().add(outgoingMoneyAZN));

                    saveAllForCard(creditorCard, debitorCard);
                }

            }


        }


        return convertToTransferResponse(creditorCard, debitorCard);
    }


    private void saveAllForCard(Card creditorCard, Card debitorCard) {

        Account accountOfCreditorCard = creditorCard.getAccount();
        accountOfCreditorCard.setBalance(creditorCard.getBalance());

        accountOfCreditorCard
                .getCardList()
                        .forEach(
                                cardOfAccount->
                                        cardOfAccount.setBalance(creditorCard.getBalance())
                        );

        accountRepository.save(accountOfCreditorCard);


        Account accountOfDebitorCard = debitorCard.getAccount();
        accountOfDebitorCard.setBalance(debitorCard.getBalance());

        accountOfDebitorCard
                .getCardList()
                        .forEach(
                                cardOfAccount->
                                        cardOfAccount.setBalance(debitorCard.getBalance())
                        );

        accountRepository.save(accountOfDebitorCard);
    }


    private TransferResponse convertToTransferResponse(Card creditorCard, Card debitorCard) {

        TransferResponse transferResponse = new TransferResponse();


        transferResponse.setTransferType(TransferTypeEnum.CARD_TO_CARD)
                .setCreditorCardID(creditorCard.getId())
                .setCreditorAccountID(creditorCard.getAccount().getId())
                .setCreditorCurrency(creditorCard.getCurrency())
                .setCreditorAmount(creditorCard.getBalance())

                .setDebitorCardID(debitorCard.getId())
                .setDebitorAccountID(debitorCard.getAccount().getId())
                .setDebitorCurrency(debitorCard.getCurrency())
                .setDebitorAmount(debitorCard.getBalance())


                .setStatus(TransferStatusEnum.SUCCESS);


        Transfer transferm = TransferMapper.MAPPER.toTransfer(transferResponse);

        Transfer savedTransfer = transferRepository.save(transferm);

        transferResponse.setId(savedTransfer.getId());


        return transferResponse;
    }
}
