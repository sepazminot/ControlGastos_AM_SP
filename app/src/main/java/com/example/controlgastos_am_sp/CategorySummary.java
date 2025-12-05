package com.example.controlgastos_am_sp;

public class CategorySummary {
    private String categoryName;
    private double totalAmount;

    public CategorySummary(String categoryName, double totalAmount) {
        this.categoryName = categoryName;
        this.totalAmount = totalAmount;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public double getTotalAmount() {
        return totalAmount;
    }

    // Opcional: Si quieres un campo extra para el porcentaje
    // public double getPercentage(double totalExpenses) {
    //     return (totalAmount / totalExpenses) * 100;
    // }

}
