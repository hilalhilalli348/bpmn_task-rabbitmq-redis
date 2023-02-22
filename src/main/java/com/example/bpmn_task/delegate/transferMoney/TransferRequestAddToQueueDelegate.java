package com.example.bpmn_task.delegate.transferMoney;

import com.example.bpmn_task.dto.request.TransferMoneyRequest;
import com.example.bpmn_task.dto.response.AccountCardMoneyContainer;
import com.example.bpmn_task.dto.response.CommonResponse;
import com.example.bpmn_task.dto.response.Status;

import com.example.bpmn_task.dto.response.TransferResponse;
import com.example.bpmn_task.entity.Account;
import com.example.bpmn_task.entity.Card;
import com.example.bpmn_task.enums.ErrorEnum;
import com.example.bpmn_task.enums.TransferTypeEnum;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

@Component("transferRequestAddToQueueDelegate")
public class TransferRequestAddToQueueDelegate implements JavaDelegate {

    @Value("${rabbitmqProperties.routing-name}")
    private String rountingName;
    @Value("${rabbitmqProperties.exchange-name}")
    private String exchangeName;

    private final RabbitTemplate rabbitTemplate;

    @Autowired
    public TransferRequestAddToQueueDelegate(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }


    @Override
    public void execute(DelegateExecution execution) throws Exception {


        TransferMoneyRequest transferMoneyRequest = (TransferMoneyRequest) execution.getVariable("transferMoneyRequest");
        TransferTypeEnum transferType = transferMoneyRequest.getTransferType();


        switch (transferType){
            case ACCOUNT_TO_CARD:{
                addToQueueTransferOfAccountToCard(execution,transferMoneyRequest);
                break;
            }

            case ACCOUNT_TO_ACCOUNT:{
                addToQueueTransferOfAccountToAccount(execution,transferMoneyRequest);
                break;
            }

            case CARD_TO_CARD:{
                addToQueueTransferOfCardToCard(execution,transferMoneyRequest);
                break;
            }

            case CARD_TO_ACCOUNT:{
                addToQueueTransferOfCardToAccount(execution,transferMoneyRequest);
            }
        }

        CommonResponse commonResponse = new CommonResponse();
        commonResponse.setStatus(Status.of(ErrorEnum.OPERATION_IS_IN_PROGRESS.getStatus(), ErrorEnum.OPERATION_IS_IN_PROGRESS.getMessage()));
        commonResponse.setHttpStatusCode(HttpStatus.OK.value());
        commonResponse.setData(null);


        execution.setVariable("response", commonResponse);

    }

    private void addToQueueTransferOfCardToAccount(DelegateExecution execution, TransferMoneyRequest transferMoneyRequest) {

        Card creditorCard = (Card) execution.getVariable("card");
        Account debitorAccount = (Account) execution.getVariable("account");

        AccountCardMoneyContainer accountCardMoneyContainer = new AccountCardMoneyContainer();
        accountCardMoneyContainer.setCreditorCard(creditorCard).setDebitorAccount(debitorAccount);
        accountCardMoneyContainer.setOutgoingMoney(transferMoneyRequest.getOutgoingMoney());
        accountCardMoneyContainer.setTransferMoneyRequest(transferMoneyRequest);

        rabbitTemplate.convertAndSend(exchangeName,rountingName,accountCardMoneyContainer);
    }

    private void addToQueueTransferOfCardToCard(DelegateExecution execution, TransferMoneyRequest transferMoneyRequest) {
        Card creditorCard = (Card) execution.getVariable("creditorCard");
        Card debitorCard = (Card) execution.getVariable("debitorCard");

        AccountCardMoneyContainer accountCardMoneyContainer = new AccountCardMoneyContainer();
        accountCardMoneyContainer.setCreditorCard(creditorCard);
        accountCardMoneyContainer.setDebitorCard(debitorCard);
        accountCardMoneyContainer.setOutgoingMoney(transferMoneyRequest.getOutgoingMoney());
        accountCardMoneyContainer.setTransferMoneyRequest(transferMoneyRequest);

        rabbitTemplate.convertAndSend(exchangeName,rountingName,accountCardMoneyContainer);
    }

    private void addToQueueTransferOfAccountToAccount(DelegateExecution execution, TransferMoneyRequest transferMoneyRequest) {

        Account creditorAccount = (Account) execution.getVariable("creditorAccount");
        Account debitorAccount = (Account) execution.getVariable("debitorAccount");

        AccountCardMoneyContainer accountCardMoneyContainer = new AccountCardMoneyContainer();

        accountCardMoneyContainer.setCreditorAccount(creditorAccount);
        accountCardMoneyContainer.setDebitorAccount(debitorAccount);
        accountCardMoneyContainer.setOutgoingMoney(transferMoneyRequest.getOutgoingMoney());
        accountCardMoneyContainer.setTransferMoneyRequest(transferMoneyRequest);

        rabbitTemplate.convertAndSend(exchangeName,rountingName,accountCardMoneyContainer);
    }

    private void addToQueueTransferOfAccountToCard(DelegateExecution execution, TransferMoneyRequest transferMoneyRequest) {
        Account account  = (Account) execution.getVariable("account");
        Card card = (Card) execution.getVariable("card");


        AccountCardMoneyContainer accountCardMoneyContainer = new AccountCardMoneyContainer();
        accountCardMoneyContainer.setCreditorAccount(account);
        accountCardMoneyContainer.setDebitorCard(card);
        accountCardMoneyContainer.setTransferMoneyRequest(transferMoneyRequest);

        rabbitTemplate.convertAndSend(exchangeName,rountingName,accountCardMoneyContainer);
    }
}
