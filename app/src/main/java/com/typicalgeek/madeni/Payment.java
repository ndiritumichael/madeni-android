package com.typicalgeek.madeni;

public class Payment {
    private int paymentID;
    private int paymentDebt;
    private float paymentAmount;
    private String paymentDate;

    Payment(int paymentID, int paymentDebt, float paymentAmount, String paymentDate) {
        this.paymentID = paymentID;
        this.paymentDebt = paymentDebt;
        this.paymentAmount = paymentAmount;
        this.paymentDate = paymentDate;
    }

    Payment(int paymentDebt, float paymentAmount, String paymentDate) {
        this.paymentDebt = paymentDebt;
        this.paymentAmount = paymentAmount;
        this.paymentDate = paymentDate;
    }

    public int getPaymentID() {
        return paymentID;
    }

    public int getPaymentDebt() {
        return paymentDebt;
    }

    public float getPaymentAmount() {
        return paymentAmount;
    }

    public String getPaymentDate() {
        return paymentDate;
    }
}
