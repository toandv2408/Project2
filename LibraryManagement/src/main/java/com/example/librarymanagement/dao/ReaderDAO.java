package com.example.librarymanagement.dao;

import com.example.librarymanagement.DatabaseConnection;
import com.example.librarymanagement.models.Book;
import com.example.librarymanagement.models.Reader;
import com.example.librarymanagement.request.ReaderRequest;

import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class ReaderDAO {

    public List<Reader> getAllReaders() {
        List<Reader> readers = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT reader_id, reader_code, full_name, email, phone_number, address, dob, gender, citizenship_card, expiry_date FROM readers WHERE status = 0")) {

            while (rs.next()) {
                Integer id = rs.getInt("reader_id");
                String readerCode = rs.getString("reader_code");
                String fullName = rs.getString("full_name");
                String email = rs.getString("email");
                String phoneNumber = rs.getString("phone_number");
                String address = rs.getString("address");
                LocalDate dob = rs.getDate("dob").toLocalDate();
                LocalDate expiryDate = rs.getDate("expiry_date").toLocalDate();
                String gender = rs.getString("gender");
                String citizenshipCard = rs.getString("citizenship_card");

                // Chỉnh sửa ở đây để truyền đúng tham số
                readers.add(new Reader(id, fullName, email, phoneNumber, address, dob, gender, citizenshipCard, readerCode, expiryDate));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return readers;
    }


    public void addReader(ReaderRequest readerRequest) {
        String insertReaderSQL = "INSERT INTO readers (full_name, email, phone_number, address, dob, gender, citizenship_card, reader_code, expiry_date) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement readerStatement = connection.prepareStatement(insertReaderSQL)) {

            // Set parameters for the reader
            readerStatement.setString(1, readerRequest.getFullName());
            readerStatement.setString(2, readerRequest.getEmail());
            readerStatement.setString(3, readerRequest.getPhoneNumber());
            readerStatement.setString(4, readerRequest.getAddress());
            readerStatement.setDate(5, java.sql.Date.valueOf(readerRequest.getDob())); // Assuming dob is LocalDate
            readerStatement.setString(6, readerRequest.getGender());
            readerStatement.setString(7, readerRequest.getCitizenshipCard());
            readerStatement.setString(8, readerRequest.getReaderCode());

            // Calculate expiry date as one year from now
            LocalDate expiryDate = LocalDate.now().plusYears(1);
            readerStatement.setDate(9, java.sql.Date.valueOf(expiryDate)); // Set expiry date to one year from now


            // Execute the insert
            readerStatement.executeUpdate();

            // Get the ID of the newly added reader
            String selectIdSQL = "SELECT reader_id FROM readers WHERE reader_code = ?";
            try (PreparedStatement selectIdStatement = connection.prepareStatement(selectIdSQL)) {
                selectIdStatement.setString(1, readerRequest.getReaderCode()); // or use another unique field
                ResultSet resultSet = selectIdStatement.executeQuery();
                if (resultSet.next()) {
                    int readerId = resultSet.getInt("reader_id");

                    // Check reader's age
                    if (calculateAge(readerRequest.getDob()) >= 18) {
                        // Add invoice if reader is over 18 years old
                        addInvoice(connection, readerId, new BigDecimal("200000"));
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void updateReader(ReaderRequest readerRequest) {
        // Giả sử bạn có một kết nối đến cơ sở dữ liệu
        String sql = "UPDATE readers SET full_name = ?, email = ?, phone_number = ?, address = ?, dob = ?, gender = ?, citizenship_card = ? WHERE reader_id = ?";
        try (PreparedStatement stmt = DatabaseConnection.getConnection().prepareStatement(sql)) {
            stmt.setString(1, readerRequest.getFullName());
            stmt.setString(2, readerRequest.getEmail());
            stmt.setString(3, readerRequest.getPhoneNumber());
            stmt.setString(4, readerRequest.getAddress());
            stmt.setDate(5, Date.valueOf(readerRequest.getDob()));
            stmt.setString(6, readerRequest.getGender());
            stmt.setString(7, readerRequest.getCitizenshipCard());
            stmt.setLong(8, readerRequest.getId());
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to update reader", e);
        }
    }


    // Method to add invoice to the database
    private void addInvoice(Connection connection, int readerId, BigDecimal amount) throws SQLException {
        String lastInvoiceCodeSQL = "SELECT invoice_code FROM payments ORDER BY payment_date DESC LIMIT 1";
        String lastInvoiceCode = null;

        try (PreparedStatement lastInvoiceStatement = connection.prepareStatement(lastInvoiceCodeSQL);
             ResultSet resultSet = lastInvoiceStatement.executeQuery()) {
            if (resultSet.next()) {
                lastInvoiceCode = resultSet.getString("invoice_code");
            }
        }

        // Chèn hóa đơn vào bảng payments
        String insertInvoiceSQL = "INSERT INTO payments (amount, payment_date, status, invoice_code) VALUES (?, CURRENT_TIMESTAMP, ?, ?)";

        try (PreparedStatement invoiceStatement = connection.prepareStatement(insertInvoiceSQL)) {
            invoiceStatement.setBigDecimal(1, amount);
            invoiceStatement.setString(2, "pending");
            invoiceStatement.setString(3, generateInvoiceCode(lastInvoiceCode));

            // Thực thi câu lệnh SQL
            invoiceStatement.executeUpdate();
        }

        // Lấy ID hóa đơn vừa tạo (có thể không an toàn trong trường hợp đồng thời)
        String getLastInvoiceIdSQL = "SELECT payment_id FROM payments ORDER BY payment_date DESC LIMIT 1";
        int paymentId = -1; // ID hóa đơn

        try (PreparedStatement getIdStatement = connection.prepareStatement(getLastInvoiceIdSQL);
             ResultSet idResultSet = getIdStatement.executeQuery()) {
            if (idResultSet.next()) {
                paymentId = idResultSet.getInt("payment_id");
            }
        }

        // Chèn thông tin vào bảng payment_details
        String insertPaymentDetailsSQL = "INSERT INTO payment_details (payment_id, reader_id) VALUES (?, ?)";
        try (PreparedStatement paymentDetailsStatement = connection.prepareStatement(insertPaymentDetailsSQL)) {
            paymentDetailsStatement.setInt(1, paymentId);
            paymentDetailsStatement.setInt(2, readerId);
            paymentDetailsStatement.executeUpdate();
        }
    }



    public String getLastReaderCode() {
        String lastReaderCode = null;

        String query = "SELECT reader_code FROM readers WHERE reader_code LIKE 'REA%' ORDER BY reader_code DESC LIMIT 1";

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            if (rs.next()) {
                lastReaderCode = rs.getString("reader_code");  // Lấy mã sách lớn nhất
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return lastReaderCode;
    }


    // Method to generate a random invoice code
    private String generateInvoiceCode(String lastInvoiceCode) {
        if (lastInvoiceCode == null || lastInvoiceCode.isEmpty()) {
            return "INV00001";  // Nếu không có hóa đơn nào trong DB
        }

        // Tách phần số ra khỏi mã hóa đơn (Ví dụ: INV00012 -> 12)
        int currentCode = Integer.parseInt(lastInvoiceCode.substring(3));
        int newCode = currentCode + 1;  // Tăng số lên

        // Sinh mã mới với định dạng INVxxxxx
        return "INV" + String.format("%05d", newCode);
    }


    // Method to calculate age based on date of birth
    private int calculateAge(LocalDate dob) {
        LocalDate currentDate = LocalDate.now();
        return currentDate.getYear() - dob.getYear(); // Calculate age based on year difference
    }

    public void deleteReader(Integer readerId) {
        String sql = "UPDATE readers SET status = 1 WHERE reader_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, readerId);
            int rowsAffected = stmt.executeUpdate();

            if (rowsAffected > 0) {
                System.out.println("Reader with ID " + readerId + " has been deleted.");
            } else {
                System.out.println("No reader found with ID " + readerId + ".");
            }
        } catch (SQLException e) {
            System.err.println("Error deleting reader: " + e.getMessage());
        }
    }
}
