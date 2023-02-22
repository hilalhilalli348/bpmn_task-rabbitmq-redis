package com.example.bpmn_task.enums;

import java.io.Serializable;

public enum TransferTypeEnum implements Serializable {
    NONE,
    CARD_TO_CARD,
    ACCOUNT_TO_ACCOUNT,
    ACCOUNT_TO_CARD,
    CARD_TO_ACCOUNT
}
