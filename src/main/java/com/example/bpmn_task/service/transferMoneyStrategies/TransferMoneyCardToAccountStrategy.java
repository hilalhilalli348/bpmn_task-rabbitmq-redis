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
public class TransferMoneyCardToAccountStrategy implements TransferMoneyStrategy{
    private final AccountRepository accountRepository;
    private final CardRepository cardRepository;
    private final TransferRepository transferRepository;

    private final CurrencyService currencyService;

    @Autowired
    public TransferMoneyCardToAccountStrategy(AccountRepository accountRepository, CardRepository cardRepository, TransferRepository transferRepository, CurrencyService currencyService) {
        this.accountRepository = accountRepository;
        this.cardRepository = cardRepository;
        this.transferRepository = transferRepository;
        this.currencyService = currencyService;
    }
    @Transactional
    @Override
    public TransferResponse transfer(AccountCardMoneyContainer accountCardMoneyContainer) {

        Card creditorCard = cardRepository.findByCardNumber(accountCardMoneyContainer.getCreditorCard().getCardNumber()).get();
        Account debitorAccount = accountRepository.findByAccountNumber(accountCardMoneyContainer.getDebitorAccount().getAccountNumber()).get();

        BigDecimal outgoingMoney = accountCardMoneyContainer.getOutgoingMoney();


        if (creditorCard.getCurrency()==debitorAccount.getCurrency()){

            creditorCard.setBalance(creditorCard.getBalance().subtract(outgoingMoney));
            debitorAccount.setBalance(debitorAccount.getBalance().add(outgoingMoney));

            saveAllChanging( creditorCard,debitorAccount);

        }

        else{

            if (creditorCard.getCurrency()== CurrencyEnum.AZN){

                if (debitorAccount.getCurrency()==CurrencyEnum.USD){

                    BigDecimal currentCurrency = currencyService.getCurrency("USD");
                    BigDecimal outgoingMoneyAZN = outgoingMoney;

                    BigDecimal outgoingMoneyUSD = outgoingMoney
                            .divide(currentCurrency, MathContext.DECIMAL32);

                    creditorCard.setBalance(creditorCard.getBalance().subtract(outgoingMoneyAZN));
                    debitorAccount.setBalance(debitorAccount.getBalance().add(outgoingMoneyUSD));

                    saveAllChanging(creditorCard,debitorAccount);
                }

            }

            else if (creditorCard.getCurrency()==CurrencyEnum.USD){

                if (debitorAccount.getCurrency()==CurrencyEnum.AZN){

                    BigDecimal currentCurrency =currencyService.getCurrency("USD");
                    BigDecimal outgoingMoneyUSD =outgoingMoney;//azn

                    BigDecimal outgoingMoneyAZN = outgoingMoney
                            .multiply(currentCurrency, MathContext.DECIMAL32);


                    creditorCard.setBalance(creditorCard.getBalance().subtract(outgoingMoneyUSD));
                    debitorAccount.setBalance(debitorAccount.getBalance().add(outgoingMoneyAZN));

                    saveAllChanging(creditorCard,debitorAccount);
                }

            }



        }


        return convertToTransferResponse(debitorAccount,creditorCard);
    }

    private void saveAllChanging(Card creditorCard ,Account debitorAccount) {


        Account accountOfCreditorCard = creditorCard.getAccount();
        accountOfCreditorCard.setBalance(creditorCard.getBalance());
        accountOfCreditorCard
                .getCardList()
                .stream()
                .forEach(
                        cardOfAccount->
                                cardOfAccount.setBalance(creditorCard.getBalance())
                );

        accountRepository.save(accountOfCreditorCard);

        debitorAccount
                .getCardList()
                .forEach(
                        cardOfDebitorAccount->
                                cardOfDebitorAccount.setBalance(debitorAccount.getBalance())
                );
        accountRepository.save(debitorAccount);
    }


    private TransferResponse convertToTransferResponse(  Account debitorAccount, Card creditorCard) {

        TransferResponse transferResponse = new TransferResponse();


        transferResponse.setTransferType(TransferTypeEnum.CARD_TO_ACCOUNT)
                .setCreditorCardID(creditorCard.getId())
                .setCreditorCurrency(creditorCard.getCurrency())
                .setCreditorAmount(creditorCard.getBalance())


                .setDebitorAccountID(debitorAccount.getId())
                .setDebitorCurrency(debitorAccount.getCurrency())
                .setDebitorAmount(debitorAccount.getBalance())


                .setStatus(TransferStatusEnum.SUCCESS);


        Transfer transferm = TransferMapper.MAPPER.toTransfer(transferResponse);

        Transfer savedTransfer = transferRepository.save(transferm);
        transferResponse.setId(savedTransfer.getId());

        return transferResponse;
    }

}
