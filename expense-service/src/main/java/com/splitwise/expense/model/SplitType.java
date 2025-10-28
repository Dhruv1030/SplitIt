package com.splitwise.expense.model;

public enum SplitType {
    EQUAL, // Split equally among all participants
    EXACT, // Each person has exact amount specified
    PERCENTAGE // Split by percentage (must total 100%)
}
