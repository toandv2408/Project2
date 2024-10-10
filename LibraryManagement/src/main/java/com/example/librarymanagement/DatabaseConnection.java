package com.example.librarymanagement;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {
    private static final String URL = "jdbc:mysql://localhost:3306/library_management"; // URL cơ sở dữ liệu
    private static final String USER = "root"; // Tên đăng nhập cơ sở dữ liệu
    private static final String PASSWORD = "12345678"; // Mật khẩu cơ sở dữ liệu

    private Connection connection;

    public DatabaseConnection() {
        try {
            connection = DriverManager.getConnection(URL, USER, PASSWORD);
            System.out.println("Kết nối cơ sở dữ liệu thành công!");
        } catch (SQLException e) {
            System.err.println("Lỗi kết nối cơ sở dữ liệu: " + e.getMessage());
        }
    }

    public static Connection getConnection() { // Thay đổi phương thức thành static
        try {
            return DriverManager.getConnection(URL, USER, PASSWORD);
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    public void closeConnection() {
        if (connection != null) {
            try {
                connection.close();
                System.out.println("Đóng kết nối cơ sở dữ liệu thành công!");
            } catch (SQLException e) {
                System.err.println("Lỗi đóng kết nối cơ sở dữ liệu: " + e.getMessage());
            }
        }
    }
}
