package com.example.librarymanagement.models;

import java.time.LocalDate;

public class BorrowBook {
    private Long id;
    private String cardCode;
    private String fullName; // Tên đầy đủ của độc giả
    private String bookCode; // Mã sách
    private LocalDate borrowedDate; // Ngày mượn
    private LocalDate dueDate; // Ngày trả dự kiến
    private LocalDate returnDate; // Ngày trả thực tế
    private int quantity; // Số lượng mượn
    private String status; // Trạng thái (Đang mượn, Đã trả, Quá hạn, ...)

    // Constructor
    public BorrowBook(Long id, String cardCode, String fullName, String bookCode,
                      LocalDate borrowedDate, LocalDate dueDate, LocalDate returnDate,
                      int quantity, String status) {
        this.id = id;
        this.cardCode = cardCode;
        this.fullName = fullName;
        this.bookCode = bookCode;
        this.borrowedDate = borrowedDate;
        this.dueDate = dueDate;
        this.returnDate = returnDate;
        this.quantity = quantity;
        this.status = status;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCardCode() {
        return cardCode;
    }

    public void setCardCode(String cardCode) {
        this.cardCode = cardCode;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getBookCode() {
        return bookCode;
    }

    public void setBookCode(String bookCode) {
        this.bookCode = bookCode;
    }

    public LocalDate getBorrowedDate() {
        return borrowedDate;
    }

    public void setBorrowedDate(LocalDate borrowedDate) {
        this.borrowedDate = borrowedDate;
    }

    public LocalDate getDueDate() {
        return dueDate;
    }

    public void setDueDate(LocalDate dueDate) {
        this.dueDate = dueDate;
    }

    public LocalDate getReturnDate() {
        return returnDate;
    }

    public void setReturnDate(LocalDate returnDate) {
        this.returnDate = returnDate;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
