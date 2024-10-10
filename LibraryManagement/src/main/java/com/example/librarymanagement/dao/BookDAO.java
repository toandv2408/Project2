package com.example.librarymanagement.dao;

import com.example.librarymanagement.DatabaseConnection;
import com.example.librarymanagement.models.Book;
import com.example.librarymanagement.request.BookRequest;
import com.example.librarymanagement.respone.AccountResponse;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class BookDAO {
    public List<Book> getAllBooks() {
        List<Book> books = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT book_id, title, author, published_year, genre_name, book_code, stock, price FROM books")) {

            while (rs.next()) {
                Long id = rs.getLong("book_id");
                String title = rs.getString("title");
                String author = rs.getString("author");
                Integer publishedYear = rs.getInt("published_year");
                String genreName = rs.getString("genre_name");
                String bookCode = rs.getString("book_code");
                Integer stock = rs.getInt("stock");
                Integer price = rs.getInt("price");

                books.add(new Book(id, title, author, publishedYear,
                        genreName, stock, price, bookCode));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return books;
    }

    public String getLastBookCode() {
        String lastBookCode = null;

        String query = "SELECT book_code FROM books WHERE book_code LIKE 'BOOK%' ORDER BY book_code DESC LIMIT 1";

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            if (rs.next()) {
                lastBookCode = rs.getString("book_code");  // Lấy mã sách lớn nhất
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return lastBookCode;
    }


    // Thêm sách mới vào cơ sở dữ liệu
    public boolean addBook(BookRequest book) {
        String query = "INSERT INTO books (title, author, published_year, genre_name, stock, price, book_code) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setString(1, book.getTitle());
            pstmt.setString(2, book.getAuthor());
            pstmt.setInt(3, book.getPublishedYear());
            pstmt.setString(4, book.getGenreName());
            pstmt.setInt(5, book.getStock());
            pstmt.setDouble(6, book.getPrice());
            pstmt.setString(7, book.getBookCode());

            int affectedRows = pstmt.executeUpdate(); // Số lượng bản ghi bị ảnh hưởng
            return affectedRows > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }


    public void deleteBook(Long id) {
        String deleteSQL = "DELETE FROM books WHERE book_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(deleteSQL)) {
            stmt.setLong(1, id);
            int rowsAffected = stmt.executeUpdate();

            if (rowsAffected > 0) {
                System.out.println("Book with ID " + id + " has been deleted.");
            } else {
                System.out.println("No book found with ID " + id + ".");
            }
        } catch (SQLException e) {
            System.err.println("Error deleting book: " + e.getMessage());
        }
    }

    public void updateBook(Book book) {
        String updateSQL = "UPDATE books SET title = ?, author = ?, published_year = ?, genre_name = ?, stock = ?, price = ? WHERE book_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(updateSQL)) {

            setBookPreparedStatement(pstmt, book);

            // Thực hiện câu lệnh SQL
            int rowsUpdated = pstmt.executeUpdate();

            if (rowsUpdated > 0) {
                System.out.println("Book updated successfully!");
            } else {
                System.out.println("Book not found with the provided ID.");
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    private void setBookPreparedStatement(PreparedStatement pstmt, Book book) throws SQLException {
        pstmt.setString(1, book.getTitle());
        pstmt.setString(2, book.getAuthor());
        pstmt.setInt(3, book.getPublishedYear());
        pstmt.setString(4, book.getGenreName());
        pstmt.setInt(5, book.getStock());
        pstmt.setInt(6, book.getPrice());
        pstmt.setLong(7, book.getId()); // Gán ID của sách vào vị trí thứ 7
    }

}
