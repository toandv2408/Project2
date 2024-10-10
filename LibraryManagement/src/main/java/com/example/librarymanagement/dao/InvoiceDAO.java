package com.example.librarymanagement.dao;

import com.example.librarymanagement.DatabaseConnection;
import com.example.librarymanagement.models.Invoice;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class InvoiceDAO {
    public List<Invoice> getPayments() {
        List<Invoice> payments = new ArrayList<>();
        String query = "SELECT p.payment_id, p.invoice_code, pd.payment_type, p.amount, p.payment_date, p.status, " +
                "CASE " +
                "   WHEN pd.payment_type = 'reader_card' THEN r.reader_code " +
                "   WHEN pd.payment_type = 'borrowing_card' THEN bc.borrowing_card_code " +
                "END AS related_code, " +
                "CASE " +
                "   WHEN pd.payment_type = 'reader_card' THEN r.full_name " +
                "   WHEN pd.payment_type = 'borrowing_card' THEN bc.full_name " +
                "END AS related_code_owner_name " +
                "FROM payments p " +
                "JOIN payment_details pd ON p.payment_id = pd.payment_id " +
                "LEFT JOIN readers r ON pd.reader_id = r.reader_id " +
                "LEFT JOIN borrowing_cards bc ON pd.borrowing_card_id = bc.borrowing_card_id";

        try (Statement stmt = DatabaseConnection.getConnection().createStatement(); ResultSet rs = stmt.executeQuery(query)) {
            while (rs.next()) {
                payments.add(new Invoice(
                        rs.getInt("payment_id"),
                        rs.getString("invoice_code"),
                        rs.getString("payment_type"),
                        rs.getInt("amount"),
                        rs.getTimestamp("payment_date"),
                        rs.getString("status"),
                        rs.getString("related_code"), // Thêm mã liên quan vào Payment
                        rs.getString("related_code_owner_name") // Thêm tên của người có mã liên quan vào Payment
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return payments;
    }

    public boolean updateInvoiceStatus(int paymentId, String status, Timestamp paymentDate) {
        String query = "UPDATE payments SET status = ?, payment_date = ? WHERE payment_id = ?";

        try (Connection conn = DatabaseConnection.getConnection(); // Giả sử bạn có phương thức để lấy kết nối
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setString(1, status); // Cập nhật trạng thái
            pstmt.setTimestamp(2, paymentDate); // Cập nhật ngày thanh toán
            pstmt.setInt(3, paymentId); // Xác định hóa đơn bằng paymentId

            return pstmt.executeUpdate() > 0; // Trả về true nếu có bản ghi nào được cập nhật
        } catch (SQLException e) {
            e.printStackTrace();
            return false; // Trả về false nếu có lỗi xảy ra
        }
    }

}
