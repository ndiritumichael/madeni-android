package com.typicalgeek.madeni;

public class Debt {
    private int debtID;
    private String debtName;
    private long debtPhone;
    private float debtAmount;
    private String debtDescription;
    private String debtType;
    private String debtDate;

    public Debt(int debtID, String debtName, long debtPhone, float debtAmount, String debtDescription, String debtType, String debtDate) {
        this.debtID = debtID;
        this.debtName = debtName;
        this.debtPhone = debtPhone;
        this.debtAmount = debtAmount;
        this.debtDescription = debtDescription;
        this.debtType = debtType;
        this.debtDate = debtDate;
    }

    public Debt(String debtName, long debtPhone, float debtAmount, String debtDescription, String debtType, String debtDate) {
        this.debtName = debtName;
        this.debtPhone = debtPhone;
        this.debtAmount = debtAmount;
        this.debtDescription = debtDescription;
        this.debtType = debtType;
        this.debtDate = debtDate;
    }

    public Debt(String debtName, long debtPhone, float debtAmount, String debtDescription, String debtType) {
        this.debtName = debtName;
        this.debtPhone = debtPhone;
        this.debtAmount = debtAmount;
        this.debtDescription = debtDescription;
        this.debtType = debtType;
    }

    public int getDebtID() {
        return debtID;
    }

    public long getDebtPhone() {
        return debtPhone;
    }

    public String getDebtName() {
        return debtName;
    }

    public String getDebtDescription() {
        return debtDescription;
    }

    public String getDebtType() {
        return debtType;
    }

    public String getDebtDate() {
        return debtDate;
    }

    public float getDebtAmount() {
        return debtAmount;
    }
}