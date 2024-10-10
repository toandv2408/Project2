package com.example.librarymanagement.models;

public class Book {
    private Long id;
    private String title;
    private String author;
    private Integer publishedYear;
    private String genreName;
    private String bookCode;
    private Integer stock;
    private Integer price;

    public Book() {
    }

        public Book(Long id, String title, String author, Integer publishedYear,
                    String genreName, Integer stock, Integer price,  String bookCode) {
            this.id = id;
            this.title = title;
            this.author = author;
            this.publishedYear = publishedYear;
            this.genreName = genreName;
            this.bookCode = bookCode;  // Sinh mã tự động dựa trên mã lớn nhất
            this.stock = stock;
            this.price = price;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public String getBookCode() {
        return bookCode;
    }

    public void setBookCode(String bookCode) {
        this.bookCode = bookCode;
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


}

