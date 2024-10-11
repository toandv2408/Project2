package com.example.librarymanagement.request;

import java.time.LocalDate;

public class BorrowCardRequest {
    private Long id;
    private String fullName;
    private String email;
    private String phoneNumber;
    private String address;
    private LocalDate dob;
    private String gender;
    private String borrowingCardCode;

    public BorrowCardRequest() {
    }

    public BorrowCardRequest(String fullName, String email, String phoneNumber, String address, LocalDate dob, String gender, String borrowingCardCode) {
        this.id = id;
        this.fullName = fullName;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.address = address;
        this.dob = dob;
        this.gender = gender;
        this.borrowingCardCode = borrowingCardCode;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public String getBorrowingCardCode() {
        return borrowingCardCode;
    }

    public void setBorrowingCardCode(String borrowingCardCode) {
        this.borrowingCardCode = borrowingCardCode;
    }
}
