package com.example.bpmn_task.dto.response;

import com.example.bpmn_task.dto.request.TransferMoneyRequest;
import com.example.bpmn_task.entity.Account;
import com.example.bpmn_task.entity.Card;

import java.io.Serializable;
import java.math.BigDecimal;

public class AccountCardMoneyContainer implements Serializable {
    private Account creditorAccount;
    private Account debitorAccount;
    private Card creditorCard;
    private Card debitorCard;
    private BigDecimal outgoingMoney;

    private TransferMoneyRequest transferMoneyRequest;

    public Account getCreditorAccount() {
        return creditorAccount;
    }

    public AccountCardMoneyContainer() {
    }

    public AccountCardMoneyContainer(Account creditorAccount, Account debitorAccount, Card creditorCard, Card debitorCard, BigDecimal outgoingMoney, TransferMoneyRequest transferMoneyRequest) {
        this.creditorAccount = creditorAccount;
        this.debitorAccount = debitorAccount;
        this.creditorCard = creditorCard;
        this.debitorCard = debitorCard;
        this.outgoingMoney = outgoingMoney;
        this.transferMoneyRequest = transferMoneyRequest;
    }

    public AccountCardMoneyContainer setCreditorAccount(Account creditorAccount) {
        this.creditorAccount = creditorAccount;
        return this;
    }

    public Account getDebitorAccount() {
        return debitorAccount;
    }

    public AccountCardMoneyContainer setDebitorAccount(Account debitorAccount) {
        this.debitorAccount = debitorAccount;
        return this;
    }

    public Card getCreditorCard() {
        return creditorCard;
    }

    public AccountCardMoneyContainer setCreditorCard(Card creditorCard) {
        this.creditorCard = creditorCard;
        return this;
    }

    public Card getDebitorCard() {
        return debitorCard;
    }

    public AccountCardMoneyContainer setDebitorCard(Card debitorCard) {
        this.debitorCard = debitorCard;
        return this;
    }

    public BigDecimal getOutgoingMoney() {
        return outgoingMoney;
    }

    public AccountCardMoneyContainer setOutgoingMoney(BigDecimal outgoingMoney) {
        this.outgoingMoney = outgoingMoney;
        return this;
    }

    public TransferMoneyRequest getTransferMoneyRequest() {
        return transferMoneyRequest;
    }

    public AccountCardMoneyContainer setTransferMoneyRequest(TransferMoneyRequest transferMoneyRequest) {
        this.transferMoneyRequest = transferMoneyRequest;
        return this;
    }
}
