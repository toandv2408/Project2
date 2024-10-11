package com.example.librarymanagement.models;

import java.time.LocalDate;

public class BorrowCard {
    private Long borrowCardId;
    private String fullName;
    private String email;
    private String phoneNumber;
    private String address;
    private LocalDate dob;
    private String gender;
    private String cardCode;
    private Integer deposit;

    public BorrowCard() {
    }

    public BorrowCard(Long borrowCardId, String fullName, String email, String phoneNumber, String address, LocalDate dob, String gender, String cardCode, Integer deposit) {
        this.borrowCardId = borrowCardId;
        this.fullName = fullName;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.address = address;
        this.dob = dob;
        this.gender = gender;
        this.cardCode = cardCode;
        this.deposit = deposit;
    }

    public Long getBorrowCardId() {
        return borrowCardId;
    }

    public void setBorrowCardId(Long borrowCardId) {
        this.borrowCardId = borrowCardId;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public LocalDate getDob() {
        return dob;
    }

    public void setDob(LocalDate dob) {
        this.dob = dob;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getCardCode() {
        return cardCode;
    }

    public void setCardCode(String cardCode) {
        this.cardCode = cardCode;
    }

    public Integer getDeposit() {
        return deposit;
    }

    public void setDeposit(Integer deposit) {
        this.deposit = deposit;
    }
}
