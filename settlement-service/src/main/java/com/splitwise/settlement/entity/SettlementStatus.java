package com.splitwise.settlement.entity;

public enum SettlementStatus {
    PENDING, // Settlement suggested but not yet paid
    COMPLETED, // Payment made and confirmed
    CANCELLED // Settlement cancelled
}
