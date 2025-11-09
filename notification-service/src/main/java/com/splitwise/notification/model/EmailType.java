package com.splitwise.notification.model;

public enum EmailType {
    PAYMENT_REMINDER, // Manually triggered or weekly scheduled
    PAYMENT_RECEIVED, // When someone pays you
    GROUP_INVITATION, // Invite someone to join a group
    WEEKLY_SUMMARY // Optional weekly digest (future)
}
