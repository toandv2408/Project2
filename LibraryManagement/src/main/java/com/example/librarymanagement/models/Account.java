package com.example.librarymanagement.models;

import java.sql.*;
import org.mindrot.jbcrypt.BCrypt;

import com.example.librarymanagement.DatabaseConnection; // Thay đổi theo package của bạn

public class Account {
    private int accountId;
    private String username;
    private String password; // Mật khẩu sẽ được mã hóa
    private String email;
    private byte[] profileImage;
    private Timestamp createdAt;
    private Timestamp updatedAt;
    private String address;
    private String phoneNumber;
    private String fullName;
    private Date dob;

    public Account() {
    }

    public Account(int accountId, String username, String password, String email, byte[] profileImage, Timestamp createdAt, Timestamp updatedAt, String address, String phoneNumber, String fullName, Date dob) {
        this.accountId = accountId;
        this.username = username;
        this.password = password;
        this.email = email;
        this.profileImage = profileImage;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.address = address;
        this.phoneNumber = phoneNumber;
        this.fullName = fullName;
        this.dob = dob;
    }

    public int getAccountId() {
        return accountId;
    }

    public void setAccountId(int accountId) {
        this.accountId = accountId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = hashPassword(password);
    }

    private String hashPassword(String password) {
        return BCrypt.hashpw(password, BCrypt.gensalt());
    }

    // Phương thức xác thực mật khẩu
    public boolean checkPassword(String password) {
        return BCrypt.checkpw(password, this.password);
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public byte[] getProfileImage() {
        return profileImage;
    }

    public void setProfileImage(byte[] profileImage) {
        this.profileImage = profileImage;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    public Timestamp getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Timestamp updatedAt) {
        this.updatedAt = updatedAt;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public Date getDob() {
        return dob;
    }

    public void setDob(Date dob) {
        this.dob = dob;
    }

    public boolean login(String username, String password) {
        try {
            Connection connection = DatabaseConnection.getConnection();
            String query = "SELECT password FROM accounts WHERE username = ?";
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            preparedStatement.setString(1, username);
            ResultSet resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {
                String hashedPassword = resultSet.getString("password");

                // So sánh mật khẩu đã nhập với mật khẩu mã hóa trong cơ sở dữ liệu
                if (BCrypt.checkpw(password, hashedPassword)) {
                    return true;  // Đăng nhập thành công
                }
            }
            return false;  // Username không tồn tại hoặc mật khẩu không đúng
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }


}
