package com.example.bpmn_task.delegate.transferMoney;

import com.example.bpmn_task.dto.request.TransferMoneyRequest;
import com.example.bpmn_task.dto.response.AccountCardMoneyContainer;
import com.example.bpmn_task.entity.Account;
import com.example.bpmn_task.entity.Card;
import com.example.bpmn_task.enums.TransferTypeEnum;
import com.example.bpmn_task.repository.AccountRepository;
import com.example.bpmn_task.service.TransferMoneyService;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class TransferMoneyConsumer {
    private final TransferMoneyService transferMoneyService;
    private final AccountRepository accountRepository;

    @Autowired
    public TransferMoneyConsumer(TransferMoneyService transferMoneyService, AccountRepository accountRepository) {
        this.transferMoneyService = transferMoneyService;
        this.accountRepository = accountRepository;
    }

    @Transactional
    @RabbitListener(queues = "${rabbitmqProperties.queue-name}")
    public void execute(AccountCardMoneyContainer accountCardMoneyContainer) {

        TransferMoneyRequest transferMoneyRequest = accountCardMoneyContainer.getTransferMoneyRequest();
        TransferTypeEnum transferType = transferMoneyRequest.getTransferType();

        switch (transferType) {
            case ACCOUNT_TO_CARD: {
                callTransferOfAccountToCard(accountCardMoneyContainer, transferMoneyRequest);
            }
            break;

            case ACCOUNT_TO_ACCOUNT: {
                callTransferOfAccountToAccount(accountCardMoneyContainer, transferMoneyRequest);
            }
            break;

            case CARD_TO_CARD: {
                callTransferOfCardToCard(accountCardMoneyContainer, transferMoneyRequest);
            }
            break;

            case CARD_TO_ACCOUNT: {
                callTransferOfCardToAccount(accountCardMoneyContainer, transferMoneyRequest);
            }
            break;
        }


    }

    @Transactional
    private void callTransferOfCardToCard(AccountCardMoneyContainer accountCardMoneyContainer, TransferMoneyRequest transferMoneyRequest) {
        accountCardMoneyContainer.setOutgoingMoney(transferMoneyRequest.getOutgoingMoney());
        transferMoneyService.transferMoneyCardToCard(accountCardMoneyContainer);
    }

    private void callTransferOfCardToAccount(AccountCardMoneyContainer accountCardMoneyContainer, TransferMoneyRequest transferMoneyRequest) {
        accountCardMoneyContainer.setOutgoingMoney(transferMoneyRequest.getOutgoingMoney());
        transferMoneyService.transferMoneyCardToAccount(accountCardMoneyContainer);
    }

    private void callTransferOfAccountToAccount(AccountCardMoneyContainer accountCardMoneyContainer, TransferMoneyRequest transferMoneyRequest) {
        accountCardMoneyContainer.setOutgoingMoney(transferMoneyRequest.getOutgoingMoney());
        transferMoneyService.transferMoneyAccountToAccount(accountCardMoneyContainer);
    }

    private void callTransferOfAccountToCard(AccountCardMoneyContainer accountCardMoneyContainer, TransferMoneyRequest transferMoneyRequest) {
        accountCardMoneyContainer.setOutgoingMoney(transferMoneyRequest.getOutgoingMoney());
        transferMoneyService.transferMoneyAccountToCard(accountCardMoneyContainer);
    }
}
