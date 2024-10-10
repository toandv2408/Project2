package com.example.librarymanagement.models;

import java.sql.Timestamp;

public class Invoice {
    private int paymentId;
    private String invoiceCode;
    private String paymentType;
    private int amount;
    private Timestamp paymentDate;
    private String status;
    private String relatedCode; // Mã liên quan
    private String relatedCodeOwnerName; // Tên của người có mã liên quan

    public Invoice() {
    }

    public Invoice(int paymentId, String invoiceCode, String paymentType, int amount, Timestamp paymentDate, String status, String relatedCode, String relatedCodeOwnerName) {
        this.paymentId = paymentId;
        this.invoiceCode = invoiceCode;
        this.paymentType = paymentType;
        this.amount = amount;
        this.paymentDate = paymentDate;
        this.status = status;
        this.relatedCode = relatedCode;
        this.relatedCodeOwnerName = relatedCodeOwnerName;
    }

    public int getPaymentId() {
        return paymentId;
    }

    public void setPaymentId(int paymentId) {
        this.paymentId = paymentId;
    }

    public String getInvoiceCode() {
        return invoiceCode;
    }

    public void setInvoiceCode(String invoiceCode) {
        this.invoiceCode = invoiceCode;
    }

    public String getPaymentType() {
        return paymentType;
    }

    public void setPaymentType(String paymentType) {
        this.paymentType = paymentType;
    }

    public int getAmount() {
        return amount;
    }

    public void setAmount(int amount) {
        this.amount = amount;
    }

    public Timestamp getPaymentDate() {
        return paymentDate;
    }

    public void setPaymentDate(Timestamp paymentDate) {
        this.paymentDate = paymentDate;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getRelatedCode() {
        return relatedCode;
    }

    public void setRelatedCode(String relatedCode) {
        this.relatedCode = relatedCode;
    }

    public String getRelatedCodeOwnerName() {
        return relatedCodeOwnerName;
    }

    public void setRelatedCodeOwnerName(String relatedCodeOwnerName) {
        this.relatedCodeOwnerName = relatedCodeOwnerName;
    }
}
