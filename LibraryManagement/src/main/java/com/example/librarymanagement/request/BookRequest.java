package com.example.librarymanagement.request;

import com.example.librarymanagement.models.Book;

public class BookRequest extends Book {
    private String title;
    private String author;
    private Integer publishedYear;
    private String genreName;
    private Integer stock;
    private Integer price;
    private String bookCode;
    public BookRequest() {
    }

    public BookRequest(String title, String author, Integer publishedYear, String genreName, Integer stock, Integer price, String bookCode) {
        this.title = title;
        this.author = author;
        this.publishedYear = publishedYear;
        this.genreName = genreName;
        this.stock = stock;
        this.price = price;
        this.bookCode = bookCode;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public Integer getPublishedYear() {
        return publishedYear;
    }

    public void setPublishedYear(Integer publishedYear) {
        this.publishedYear = publishedYear;
    }

    public String getGenreName() {
        return genreName;
    }

    public void setGenreName(String genreName) {
        this.genreName = genreName;
    }

    public Integer getStock() {
        return stock;
    }

    public void setStock(Integer stock) {
        this.stock = stock;
    }

    public Integer getPrice() {
        return price;
    }

    public void setPrice(Integer price) {
        this.price = price;
    }

    @Override
    public String getBookCode() {
        return bookCode;
    }

    @Override
    public void setBookCode(String bookCode) {
        this.bookCode = bookCode;
    }
}
