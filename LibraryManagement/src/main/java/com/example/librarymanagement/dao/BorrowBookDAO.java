package com.example.librarymanagement.dao;

import com.example.librarymanagement.DatabaseConnection;
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



}
