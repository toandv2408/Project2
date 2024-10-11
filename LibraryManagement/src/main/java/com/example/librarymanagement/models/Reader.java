package com.example.librarymanagement.models;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;

import java.time.LocalDate;
import java.time.Period;
import java.sql.Date;

public class Reader {

    private Long readerId;
    private String fullName;
    private String email;
    private String phoneNumber;
    private String address;
    private LocalDate  dob;
    private String gender;
    private String citizenshipCard;
    private String readerCode;
    private LocalDate  expiryDate;

    // Constructor

    public Reader() {
    }

    public Reader(Long readerId, String fullName, String email, String phoneNumber, String address, LocalDate dob, String gender, String citizenshipCard, String readerCode, LocalDate expiryDate) {
        this.readerId = readerId;
        this.fullName = fullName;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.address = address;
        this.dob = dob;
        this.gender = gender;
        this.citizenshipCard = citizenshipCard;
        this.readerCode = readerCode;
        this.expiryDate = expiryDate;
    }

    public Long getReaderId() {
        return readerId;
    }

    public void setReaderId(Long readerId) {
        this.readerId = readerId;
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

    public String getCitizenshipCard() {
        return citizenshipCard;
    }

    public void setCitizenshipCard(String citizenshipCard) {
        this.citizenshipCard = citizenshipCard;
    }

    public String getReaderCode() {
        return readerCode;
    }

    public void setReaderCode(String readerCode) {
        this.readerCode = readerCode;
    }

    public LocalDate  getExpiryDate() {
        return expiryDate;
    }

    public void setExpiryDate(LocalDate  expiryDate) {
        this.expiryDate = expiryDate;
    }

}
