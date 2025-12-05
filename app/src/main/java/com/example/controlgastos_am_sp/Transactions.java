package com.example.controlgastos_am_sp;

public class Transactions {

    public int id;
    public double amount;
    public int category;
    public String description;
    public long date;
    public String paymentMethod;
    public long createdAt;

    public Transactions(double amount, int category, String description, long date, String paymentMethod, long createdAt) {
        this.amount = amount;
        this.category = category;
        this.description = description;
        this.date = date;
        this.paymentMethod = paymentMethod;
        this.createdAt = createdAt;
    }

    public Transactions(double amount, int category, String description, long date, String paymentMethod) {
        this.amount = amount;
        this.category = category;
        this.description = description;
        this.date = date;
        this.paymentMethod = paymentMethod;
    }

    public Transactions() {

    }
}
