package com.splitwise.notification.model;

public enum ActivityType {
    // Expense activities
    EXPENSE_ADDED,
    EXPENSE_UPDATED,
    EXPENSE_DELETED,

    // Group activities
    GROUP_CREATED,
    MEMBER_ADDED,
    MEMBER_REMOVED,
    GROUP_UPDATED,

    // Settlement activities
    SETTLEMENT_RECORDED,
    PAYMENT_COMPLETED,

    // Member activities
    USER_JOINED,
    USER_LEFT
}
