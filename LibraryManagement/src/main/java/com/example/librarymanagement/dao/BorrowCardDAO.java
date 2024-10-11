package com.example.librarymanagement.dao;

import com.example.librarymanagement.DatabaseConnection;
import com.example.librarymanagement.models.BorrowCard;
import com.example.librarymanagement.models.Reader;
import com.example.librarymanagement.request.BorrowCardRequest;
import com.example.librarymanagement.request.ReaderRequest;

import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class BorrowCardDAO {
    public List<BorrowCard> getAllBorrowCards() {
        List<BorrowCard> borrowCards = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT borrowing_card_id, borrowing_card_code, full_name, email, phone_number, address, dob, gender, deposit FROM borrowing_cards WHERE status = 0")) {

            while (rs.next()) {
                Long id = rs.getLong("borrowing_card_id");
                String cardCode = rs.getString("borrowing_card_code");
                String fullName = rs.getString("full_name");
                String email = rs.getString("email");
                String phoneNumber = rs.getString("phone_number");
                String address = rs.getString("address");
                LocalDate dob = rs.getDate("dob").toLocalDate();
                String gender = rs.getString("gender");
                Integer deposit = rs.getInt("deposit");
                // Chỉnh sửa ở đây để truyền đúng tham số
                borrowCards.add(new BorrowCard(id, fullName, email, phoneNumber, address, dob, gender, cardCode, deposit));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return borrowCards;
    }

    public BorrowCard getBorrowCardById(Long borrowCardId) {
        BorrowCard borrowCard = null;
        String sql = "SELECT borrowing_card_id, borrowing_card_code, full_name, email, phone_number, address, dob, gender, deposit FROM borrowing_cards WHERE borrowing_card_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, borrowCardId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Long id = rs.getLong("borrowing_card_id");
                    String cardCode = rs.getString("borrowing_card_code");
                    String fullName = rs.getString("full_name");
                    String email = rs.getString("email");
                    String phoneNumber = rs.getString("phone_number");
                    String address = rs.getString("address");
                    LocalDate dob = rs.getDate("dob").toLocalDate();
                    String gender = rs.getString("gender");
                    Integer deposit = rs.getInt("deposit");

                    borrowCard = new BorrowCard(id, fullName, email, phoneNumber, address, dob, gender, cardCode, deposit);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return borrowCard;
    }


    public void addBorrowCard(BorrowCardRequest borrowCardRequest) {
        String insertReaderSQL = "INSERT INTO borrowing_cards (full_name, email, phone_number, address, dob, gender, borrowing_card_code, deposit) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement borrowCardStatement = connection.prepareStatement(insertReaderSQL)) {

            // Set parameters for the reader
            borrowCardStatement.setString(1, borrowCardRequest.getFullName());
            borrowCardStatement.setString(2, borrowCardRequest.getEmail());
            borrowCardStatement.setString(3, borrowCardRequest.getPhoneNumber());
            borrowCardStatement.setString(4, borrowCardRequest.getAddress());
            borrowCardStatement.setDate(5, java.sql.Date.valueOf(borrowCardRequest.getDob())); // Assuming dob is LocalDate
            borrowCardStatement.setString(6, borrowCardRequest.getGender());
            borrowCardStatement.setString(7, borrowCardRequest.getBorrowingCardCode());
            borrowCardStatement.setInt(8, 700000);

            // Execute the insert
            borrowCardStatement.executeUpdate();

            // Get the ID of the newly added reader
            String selectIdSQL = "SELECT borrowing_card_id FROM borrowing_cards WHERE borrowing_card_code = ?";
            try (PreparedStatement selectIdStatement = connection.prepareStatement(selectIdSQL)) {
                selectIdStatement.setString(1, borrowCardRequest.getBorrowingCardCode()); // or use another unique field
                ResultSet resultSet = selectIdStatement.executeQuery();
                if (resultSet.next()) {
                    int borrowingCardId = resultSet.getInt("borrowing_card_id");

                        addInvoice(connection, borrowingCardId, new BigDecimal(700000));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void updateBorrowCard(BorrowCardRequest borrowCardRequest) {
        // Giả sử bạn có một kết nối đến cơ sở dữ liệu
        String sql = "UPDATE borrowing_cards SET full_name = ?, email = ?, phone_number = ?, address = ?, dob = ?, gender = ? WHERE borrowing_card_id = ?";
        try (PreparedStatement stmt = DatabaseConnection.getConnection().prepareStatement(sql)) {
            stmt.setString(1, borrowCardRequest.getFullName());
            stmt.setString(2, borrowCardRequest.getEmail());
            stmt.setString(3, borrowCardRequest.getPhoneNumber());
            stmt.setString(4, borrowCardRequest.getAddress());
            stmt.setDate(5, Date.valueOf(borrowCardRequest.getDob()));
            stmt.setString(6, borrowCardRequest.getGender());
            stmt.setLong(7, borrowCardRequest.getId());
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to update borrow card", e);
        }
    }

    public void deleteBorrowCard(Long borrowCardId) {
        String sql = "UPDATE borrowing_cards SET status = 1 WHERE borrowing_card_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            // Kiểm tra tiền cọc của BorrowCard trước khi xóa
            BorrowCard borrowCard = getBorrowCardById(borrowCardId);
            if (borrowCard == null) {
                System.out.println("No borrow card found with ID " + borrowCardId + ".");
                return;
            }

            BigDecimal depositAmount = BigDecimal.valueOf(borrowCard.getDeposit());

            // Cập nhật trạng thái của BorrowCard
            stmt.setLong(1, borrowCardId);
            int rowsAffected = stmt.executeUpdate();

            if (rowsAffected > 0) {
                System.out.println("Borrow card with ID " + borrowCardId + " has been deleted.");

                // Tạo hóa đơn hoàn trả tiền cọc
                createRefundInvoice(conn, borrowCardId, depositAmount);
            } else {
                System.out.println("No borrow card found with ID " + borrowCardId + ".");
            }
        } catch (SQLException e) {
            System.err.println("Error deleting borrow card: " + e.getMessage());
        }
    }

    private void createRefundInvoice(Connection connection, Long borrowCardId, BigDecimal refundAmount) throws SQLException {
        // Lấy mã hóa đơn cuối cùng
        String lastInvoiceCodeSQL = "SELECT invoice_code FROM payments ORDER BY payment_date DESC LIMIT 1";
        String lastInvoiceCode = null;

        try (PreparedStatement lastInvoiceStatement = connection.prepareStatement(lastInvoiceCodeSQL);
             ResultSet resultSet = lastInvoiceStatement.executeQuery()) {
            if (resultSet.next()) {
                lastInvoiceCode = resultSet.getString("invoice_code");
            }
        }

        // Chèn hóa đơn hoàn trả vào bảng payments
        String insertInvoiceSQL = "INSERT INTO payments (amount, payment_date, status, invoice_code) VALUES (?, CURRENT_TIMESTAMP, ?, ?)";

        try (PreparedStatement invoiceStatement = connection.prepareStatement(insertInvoiceSQL)) {
            invoiceStatement.setBigDecimal(1, refundAmount);
            invoiceStatement.setString(2, "refunded"); // Trạng thái là 'refunded'
            invoiceStatement.setString(3, generateInvoiceCode(lastInvoiceCode));

            // Thực thi câu lệnh SQL
            invoiceStatement.executeUpdate();
        }

        // Lấy ID của hóa đơn hoàn trả vừa tạo
        String getLastInvoiceIdSQL = "SELECT payment_id FROM payments ORDER BY payment_date DESC LIMIT 1";
        int paymentId = -1;

        try (PreparedStatement getIdStatement = connection.prepareStatement(getLastInvoiceIdSQL);
             ResultSet idResultSet = getIdStatement.executeQuery()) {
            if (idResultSet.next()) {
                paymentId = idResultSet.getInt("payment_id");
            }
        }

        // Chèn thông tin vào bảng payment_details
        String insertPaymentDetailsSQL = "INSERT INTO payment_details (payment_id, borrowing_card_id, payment_type) VALUES (?, ?, ?)";
        try (PreparedStatement paymentDetailsStatement = connection.prepareStatement(insertPaymentDetailsSQL)) {
            paymentDetailsStatement.setInt(1, paymentId);
            paymentDetailsStatement.setLong(2, borrowCardId);
            paymentDetailsStatement.setString(3, "refund_deposit"); // Loại thanh toán là 'refund_deposit'
            paymentDetailsStatement.executeUpdate();
        }
    }


    private void addInvoice(Connection connection, int borrowCardId, BigDecimal amount) throws SQLException {
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
        String insertPaymentDetailsSQL = "INSERT INTO payment_details (payment_id, borrowing_card_id, payment_type) VALUES (?, ?, ?)";
        try (PreparedStatement paymentDetailsStatement = connection.prepareStatement(insertPaymentDetailsSQL)) {
            paymentDetailsStatement.setInt(1, paymentId);
            paymentDetailsStatement.setInt(2, borrowCardId);
            paymentDetailsStatement.setString(3, "borrowing_card");
            paymentDetailsStatement.executeUpdate();
        }
    }



    public String getLastCardCode() {
        String lastReaderCode = null;

        String query = "SELECT borrowing_card_code FROM borrowing_cards WHERE borrowing_card_code LIKE 'BRC%' ORDER BY borrowing_card_code DESC LIMIT 1";

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            if (rs.next()) {
                lastReaderCode = rs.getString("borrowing_card_code");  // Lấy mã sách lớn nhất
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

}
