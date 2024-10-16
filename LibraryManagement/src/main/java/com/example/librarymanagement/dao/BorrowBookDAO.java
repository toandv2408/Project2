package com.example.librarymanagement.dao;

import com.example.librarymanagement.DatabaseConnection;
import com.example.librarymanagement.models.Book;
import com.example.librarymanagement.models.BorrowBook;
import com.example.librarymanagement.models.BorrowCard;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class BorrowBookDAO {
    public List<BorrowBook> getAllBorrowingRecords() {
        List<BorrowBook> records = new ArrayList<>();
        String sql = "SELECT br.borrowing_id, bc.borrowing_card_code, bc.full_name, b.book_code, " +
                "br.borrow_date, br.due_date, br.return_date, br.quantity, br.status " +
                "FROM borrowing_records br " +
                "JOIN borrowing_cards bc ON br.borrowing_card_id = bc.borrowing_card_id " +
                "JOIN books b ON br.book_id = b.book_id"; // JOIN để lấy thông tin sách

        try (PreparedStatement statement = DatabaseConnection.getConnection().prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                BorrowBook record = new BorrowBook(
                        resultSet.getLong("borrowing_id"),
                        resultSet.getString("borrowing_card_code"), // cardCode
                        resultSet.getString("full_name"), // fullName
                        resultSet.getString("book_code"), // bookCode
                        resultSet.getDate("borrow_date") != null ? resultSet.getDate("borrow_date").toLocalDate() : null, // borrowedDate
                        resultSet.getDate("due_date") != null ? resultSet.getDate("due_date").toLocalDate() : null, // dueDate
                        resultSet.getDate("return_date") != null ? resultSet.getDate("return_date").toLocalDate() : null, // returnDate
                        resultSet.getInt("quantity"), // quantity
                        resultSet.getString("status") // status
                );
                records.add(record);
            }
        } catch (SQLException e) {
            // In thông tin lỗi ra console
            System.err.println("SQL Exception: " + e.getMessage());
        }

        return records;
    }

    public List<String> getAllBookCodes() {
        List<String> bookCodes = new ArrayList<>();
        String query = "SELECT book_code FROM books";

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            while (rs.next()) {
                String bookCode = rs.getString("book_code");
                bookCodes.add(bookCode);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return bookCodes;
    }

    public Book getBookByCode(String bookCode) {
        Book book = null;
        String query = "SELECT * FROM books WHERE book_code = ? AND stock > 1";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setString(1, bookCode);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                Long id = rs.getLong("book_id");
                String title = rs.getString("title");
                String author = rs.getString("author");
                Integer publishedYear = rs.getInt("published_year");
                String genreName = rs.getString("genre_name");
                Integer stock = rs.getInt("stock");
                Integer price = rs.getInt("price");

                book = new Book(id, title, author, publishedYear, genreName, stock, price, bookCode);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return book;
    }

    public List<String> getAllCardCodes() {
        List<String> cardCodes = new ArrayList<>();
        String query = "SELECT borrowing_card_code FROM borrowing_cards";

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            while (rs.next()) {
                String cardCode = rs.getString("borrowing_card_code");
                cardCodes.add(cardCode);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return cardCodes;
    }
    public BorrowCard getFullNameByCode(String cardCode) {
        BorrowCard card = null;
        String query = "SELECT * FROM borrowing_cards WHERE borrowing_card_code = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setString(1, cardCode);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                String fullName = rs.getString("full_name");
                Long id = rs.getLong("borrowing_card_id");
                card = new BorrowCard(fullName, id);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return card;
    }

    private static final String INSERT_BORROW_RECORD = "INSERT INTO borrowing_records (book_id, borrowing_card_id, borrow_date, due_date, status, quantity) VALUES (?, ?, ?, ?, ?, ?)";
    private static final String UPDATE_BOOK_STOCK = "UPDATE books SET stock = ? WHERE book_id = ?";

    public boolean saveBorrowBook(BorrowBook borrowBook) {
        Connection conn = null;
        PreparedStatement pstmt = null;

        try {
            // Bước 1: Kết nối đến cơ sở dữ liệu
            conn = DatabaseConnection.getConnection();
            conn.setAutoCommit(false); // Tắt auto-commit để có thể kiểm soát transaction

            // Bước 2: Insert vào bảng borrowing_records
            pstmt = conn.prepareStatement(INSERT_BORROW_RECORD);
            pstmt.setLong(1, borrowBook.getBook().getId());
            pstmt.setLong(2, borrowBook.getBorrowCardId());
            pstmt.setDate(3, Date.valueOf(borrowBook.getBorrowedDate()));  // Ngày mượn
            pstmt.setDate(4, Date.valueOf(borrowBook.getDueDate()));        // Ngày đáo hạn
            pstmt.setString(5, borrowBook.getStatus());                    // Trạng thái (mặc định là 'active')
            pstmt.setInt(6, borrowBook.getQuantity());                     // Số lượng sách mượn

            int affectedRows = pstmt.executeUpdate();
            if (affectedRows == 0) {
                conn.rollback();
                throw new SQLException("Creating borrowing record failed, no rows affected.");
            }

            // Bước 3: Commit giao dịch
            conn.commit();
            return true;

        } catch (SQLException e) {
            // Xử lý lỗi và rollback nếu cần
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
            e.printStackTrace();
            return false;
        } finally {
            if (pstmt != null) {
                try {
                    pstmt.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
            if (conn != null) {
                try {
                    conn.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }


    // Method to update book stock after borrowing
    public void updateBookStock(Book book) {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(UPDATE_BOOK_STOCK)) {

            stmt.setInt(1, book.getStock());
            stmt.setLong(2, book.getId());
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
