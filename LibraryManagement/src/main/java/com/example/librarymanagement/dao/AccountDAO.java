package com.example.librarymanagement.dao;

import com.example.librarymanagement.DatabaseConnection;
import com.example.librarymanagement.models.Account;
import com.example.librarymanagement.request.AccountRequest;
import com.example.librarymanagement.respone.AccountResponse;
import org.mindrot.jbcrypt.BCrypt;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class AccountDAO {
    public Account getAccountById(int accountId) {
        Account account = null;
        String query = "SELECT * FROM accounts WHERE account_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, accountId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                account = new Account();
                account.setAccountId(rs.getInt("account_id"));
                account.setUsername(rs.getString("username"));
                account.setPassword(rs.getString("password"));
                account.setEmail(rs.getString("email"));
                account.setProfileImage(rs.getBytes("profile_image")); // Sử dụng getBytes để lấy byte[]
                account.setCreatedAt(rs.getTimestamp("created_at"));
                account.setUpdatedAt(rs.getTimestamp("updated_at"));
                account.setAddress(rs.getString("address"));
                account.setPhoneNumber(rs.getString("phone_number"));
                account.setFullName(rs.getString("full_name"));
                account.setDob(rs.getDate("dob"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return account;
    }

    public Account getAccountByUsername(String username) {
        Account account = null;
        String query = "SELECT * FROM accounts WHERE username = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                account = new Account();
                account.setAccountId(rs.getInt("account_id"));
                account.setUsername(rs.getString("username"));
                account.setPassword(rs.getString("password"));
                account.setEmail(rs.getString("email"));
                account.setProfileImage(rs.getBytes("profile_image")); // Lấy ảnh
                account.setCreatedAt(rs.getTimestamp("created_at"));
                account.setUpdatedAt(rs.getTimestamp("updated_at"));
                account.setAddress(rs.getString("address"));
                account.setPhoneNumber(rs.getString("phone_number"));
                account.setFullName(rs.getString("full_name"));
                account.setDob(rs.getDate("dob"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return account;
    }

    public List<AccountResponse> getAllAccounts(){
        List<AccountResponse> accounts = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT account_id, full_name, username, dob, email, phone_number, address FROM accounts")) {

            while (rs.next()) {
                Integer id = rs.getInt("account_id");
                String fullName = rs.getString("full_name");
                String username = rs.getString("username");
                String dob = rs.getString("dob");
                String email = rs.getString("email");
                String phoneNumber = rs.getString("phone_number");
                String address = rs.getString("address");

                accounts.add(new AccountResponse(id, fullName, username, dob, email, phoneNumber, address));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return accounts;
    }
    public boolean addAccount(AccountRequest accountRequest) {
        // Kết nối đến cơ sở dữ liệu và thêm tài khoản
        try (Connection connection = DatabaseConnection.getConnection()) {
            String sql = "INSERT INTO accounts (full_name, username, dob, email, phone_number, address, password, profile_image) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setString(1, accountRequest.getFullName());
            statement.setString(2, accountRequest.getUsername());
            statement.setString(3, accountRequest.getDateOfBirth());
            statement.setString(4, accountRequest.getEmail());
            statement.setString(5, accountRequest.getPhoneNumber());
            statement.setString(6, accountRequest.getAddress());
            statement.setString(7, accountRequest.getPassword());
            statement.setBytes(8, accountRequest.getAvatar());

            return statement.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean isUsernameOrEmailTaken(String username, String email, String phone) {
        try (Connection connection = DatabaseConnection.getConnection()) {
            String sql = "SELECT COUNT(*) FROM accounts WHERE username = ? OR email = ? OR phone_number = ?";
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setString(1, username);
            statement.setString(2, email);
            statement.setString(3, phone);

            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                return resultSet.getInt(1) > 0; // Nếu số lượng lớn hơn 0, có nghĩa là đã tồn tại
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false; // Không tồn tại
    }


    public boolean updateAccount(int accountId, String fullName, String username, String dob, String email, String phoneNumber, String address) {
        String sql = "UPDATE accounts SET full_name = ?, username = ?, dob = ?, email = ?, phone_number = ?, address = ? WHERE account_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, fullName);
            pstmt.setString(2, username);
            pstmt.setString(3, dob);
            pstmt.setString(4, email);
            pstmt.setString(5, phoneNumber);
            pstmt.setString(6, address);
            pstmt.setInt(7, accountId); // Sử dụng account_id để xác định bản ghi

            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0; // Trả về true nếu có ít nhất 1 bản ghi được cập nhật
        } catch (SQLException e) {
            e.printStackTrace();
            return false; // Nếu có lỗi xảy ra
        }
    }

    public void deleteAccount(int accountId) throws SQLException {
        String deleteSQL = "DELETE FROM accounts WHERE account_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(deleteSQL)) {
            stmt.setInt(1, accountId);
            int rowsAffected = stmt.executeUpdate();

            if (rowsAffected > 0) {
                System.out.println("Account with ID " + accountId + " has been deleted.");
            } else {
                System.out.println("No account found with ID " + accountId + ".");
            }
        } catch (SQLException e) {
            System.err.println("Error deleting account: " + e.getMessage());
            throw e;  // Ném lại ngoại lệ để xử lý ở nơi khác nếu cần
        }
    }

    public void changeImage(int accountId, byte[] imageBytes) {
        String sql = "UPDATE accounts SET profile_image = ? WHERE account_id = ?";

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            // Gán giá trị ảnh dưới dạng byte array
            statement.setBytes(1, imageBytes);
            // Gán giá trị accountId
            statement.setInt(2, accountId);

            // Thực thi câu lệnh update
            int rowsAffected = statement.executeUpdate();

            // Kiểm tra số dòng bị ảnh hưởng
            if (rowsAffected > 0) {
                System.out.println("Image updated successfully for account ID: " + accountId);
            } else {
                System.out.println("No rows affected. Check if account ID exists: " + accountId);
            }
        } catch (SQLException e) {
            // Xử lý ngoại lệ SQL
            System.err.println("Error while updating image for account ID: " + accountId);
            e.printStackTrace();
        }
    }

    public boolean changePassword(int accountId, String currentPassword, String newPassword) {
        // Kiểm tra mật khẩu hiện tại
        // (giả sử bạn đã có phương thức để kiểm tra mật khẩu)
        if (!isCurrentPasswordCorrect(accountId, currentPassword)) {
            return false; // Mật khẩu hiện tại không đúng
        }
        String hashedNewPassword = hashPassword(newPassword);
        // Cập nhật mật khẩu mới
        String sql = "UPDATE accounts SET password = ? WHERE account_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, hashedNewPassword); // Gán mật khẩu mới
            pstmt.setInt(2, accountId); // Gán ID tài khoản

            pstmt.executeUpdate(); // Thực hiện cập nhật
            return true; // Đổi mật khẩu thành công
        } catch (SQLException e) {
            e.printStackTrace();
            return false; // Có lỗi trong quá trình cập nhật
        }
    }

    private boolean isCurrentPasswordCorrect(int accountId, String currentPassword) {
        String sql = "SELECT password FROM accounts WHERE account_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, accountId); // Gán ID tài khoản

            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                String storedPasswordHash = rs.getString("password");
                // Kiểm tra nếu mật khẩu chưa được mã hóa
                if (storedPasswordHash == null || storedPasswordHash.isEmpty()) {
                    return currentPassword.equals(storedPasswordHash); // So sánh trực tiếp
                } else {
                    return checkPassword(currentPassword, storedPasswordHash); // Kiểm tra mật khẩu
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false; // Nếu không tìm thấy tài khoản hoặc xảy ra lỗi
    }


    private String hashPassword(String password) {
        // Sử dụng thư viện như BCrypt để mã hóa
        return BCrypt.hashpw(password, BCrypt.gensalt());
    }

    // Phương thức để kiểm tra mật khẩu
    private boolean checkPassword(String password, String storedHash) {
        // Sử dụng thư viện như BCrypt để kiểm tra
        return BCrypt.checkpw(password, storedHash);
    }


}
