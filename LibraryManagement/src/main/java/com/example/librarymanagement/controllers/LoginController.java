package com.example.librarymanagement.controllers;

import com.example.librarymanagement.dao.AccountDAO;
import com.example.librarymanagement.models.Account;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.control.Alert;
import javafx.stage.Stage;

public class LoginController {
    @FXML
    private TextField usernameField;
    @FXML
    private PasswordField passwordField;
    @FXML
    private Label errorMessage;

    @FXML
    private void handleLogin(ActionEvent event) {
        String username = usernameField.getText();
        String password = passwordField.getText();
        AccountDAO accountDAO = new AccountDAO();
        Account user = accountDAO.getAccountByUsername(username);

        if (user != null && user.login(username, password)) {
            System.out.println("Đăng nhập thành công!");

            // Nếu đăng nhập thành công, chuyển đến MainController và thiết lập ID tài khoản
            MainController mainController = (MainController) loadMainView();
            mainController.setCurrentAccountId(user.getAccountId()); // Gọi phương thức để thiết lập ID tài khoản
            mainController.loadDashboardView();
        } else {
            errorMessage.setText("Username or Password Incorrect!");
        }
    }

    private MainController loadMainView() {
        try {
            // Sử dụng đường dẫn tương đối để tải tệp FXML
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/librarymanagement/fxml/MainLayout.fxml"));
            Parent mainView = loader.load();
            MainController mainController = loader.getController();

            // Gọi phương thức setUsername để hiển thị tên người dùng
            mainController.setUsername(usernameField.getText());
            Stage stage = (Stage) usernameField.getScene().getWindow();
            stage.setScene(new Scene(mainView, 1300, 700));
            stage.setTitle("Library Management System");
            stage.centerOnScreen();
            stage.show();
            return mainController;
        } catch (Exception e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR, "Lỗi khi tải giao diện chính: " + e.getMessage());
            alert.showAndWait();
            return null;
        }
    }


}
