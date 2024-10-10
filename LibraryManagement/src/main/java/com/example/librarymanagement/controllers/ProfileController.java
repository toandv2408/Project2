package com.example.librarymanagement.controllers;

import com.example.librarymanagement.dao.AccountDAO;
import com.example.librarymanagement.models.Account;
import javafx.animation.FadeTransition;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.*;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;


import java.io.*;

import javafx.util.Duration;

import java.nio.file.Files;
import java.text.ParseException;
import java.text.SimpleDateFormat;

public class ProfileController {
    @FXML
    private VBox notificationBox;

    @FXML
    private Label notificationLabel;
    @FXML
    private ImageView profileImageView;
    @FXML
    private Label fullName;
    @FXML
    private TextField usernameField;

    @FXML
    private TextField emailField;

    @FXML
    private TextField fullNameField;

    @FXML
    private TextField addressField;

    @FXML
    private TextField phoneNumberField;

    @FXML
    private TextField dobField;

    @FXML
    private Button updateButton;
    @FXML
    private PasswordField currentPasswordField;

    @FXML
    private PasswordField newPasswordField;

    @FXML
    private PasswordField confirmPasswordField;
    @FXML
    private Button changeImageButton;
    @FXML
    private Button saveButton;
    @FXML
    private Button saveChangePassword;
    private boolean isEditing = false;
    private Account currentUser;

    private boolean isImageChanged = false;

    private int accountId;
    private File selectedImageFile;

    private AccountDAO accountDAO = new AccountDAO();
    // Gọi hàm này để tải thông tin tài khoản khi mở trang
    @FXML
    public void setAccountId(int accountId) {
        this.accountId = accountId;
        loadProfile(accountId); // Gọi để tải thông tin hồ sơ
    }
    public void loadProfile(int accountId) {
        AccountDAO accountDAO = new AccountDAO();
        currentUser = accountDAO.getAccountById(accountId);
        if (currentUser != null) {
            fullName.setText(currentUser.getFullName());
            usernameField.setText(currentUser.getUsername());
            emailField.setText(currentUser.getEmail());
            fullNameField.setText(currentUser.getFullName());
            addressField.setText(currentUser.getAddress());
            phoneNumberField.setText(currentUser.getPhoneNumber());
            dobField.setText(currentUser.getDob().toString());
            displayProfileImage(accountId);
        }
    }

    public void displayProfileImage(int accountId) {
        AccountDAO accountDAO = new AccountDAO();
        Account account = accountDAO.getAccountById(accountId); // Lấy thông tin tài khoản

        if (account != null) {
            byte[] imageBytes = account.getProfileImage();

            if (imageBytes != null) {
                try {
                    ByteArrayInputStream inputStream = new ByteArrayInputStream(imageBytes);
                    Image image = new Image(inputStream);

                    profileImageView.setImage(image);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @FXML
    void handleChangeImageAction(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Chọn ảnh hồ sơ");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg"));

        selectedImageFile = fileChooser.showOpenDialog(null);
        if (selectedImageFile != null) {
            // Hiển thị ảnh đã chọn trong ImageView
            Image image = new Image(selectedImageFile.toURI().toString());
            profileImageView.setImage(image);
        }
    }

    @FXML
    void handleSaveAction(ActionEvent event) {
        if (selectedImageFile != null) {
            try {
                // Chuyển đổi ảnh thành mảng byte
                byte[] imageBytes = Files.readAllBytes(selectedImageFile.toPath());

                // Gọi phương thức DAO để cập nhật ảnh vào database
                int accountId = currentUser.getAccountId(); // Bạn sẽ thay bằng ID của tài khoản hiện tại
                accountDAO.changeImage(accountId, imageBytes);
                showNotification("Profile image updated successfully!", "#2ECC71"); // Màu xanh lá
                System.out.println("Cập nhật ảnh thành công.");
            } catch (IOException e) {
                e.printStackTrace();
                showNotification("An error occurred while updating the profile image.", "#E74C3C"); // Màu đỏ;
            }
        } else {
            System.out.println("Bạn chưa chọn ảnh.");
            showNotification("Please select an image before saving.", "#E67E22");
        }
    }

    private void showNotification(String message, String color) {
        notificationLabel.setText(message);
        notificationBox.setStyle("-fx-background-color: " + color + ";");

        // Hiển thị thông báo
        notificationBox.setOpacity(1.0);

        // Tạo hiệu ứng chuyển động
        FadeTransition fadeOut = new FadeTransition(Duration.seconds(4), notificationBox);
        fadeOut.setFromValue(1.0);
        fadeOut.setToValue(0.0);
        fadeOut.setOnFinished(event -> notificationBox.setOpacity(0)); // Ẩn thông báo sau khi hoàn thành
        fadeOut.play();
    }
    // Xử lý khi nhấn nút Update/Save
    @FXML
    private void handleUpdateAction() {
        if (!isEditing) {
            // Chuyển sang chế độ chỉnh sửa
            enableEditing(true);
            updateButton.setText("Save");
            isEditing = true;
        } else {
            // Lưu thông tin đã chỉnh sửa
            saveProfileChanges();
            enableEditing(false);
            updateButton.setText("Update");
            isEditing = false;
        }
    }

    // Kích hoạt hoặc vô hiệu hóa khả năng chỉnh sửa các ô input
    private void enableEditing(boolean enable) {
        usernameField.setEditable(enable);
        emailField.setEditable(enable);
        fullNameField.setEditable(enable);
        addressField.setEditable(enable);
        phoneNumberField.setEditable(enable);
        dobField.setEditable(enable);
    }

    // Lưu thông tin đã chỉnh sửa vào cơ sở dữ liệu
    private void saveProfileChanges() {
        String fullName = fullNameField.getText();
        String username = usernameField.getText();
        String dob = dobField.getText();
        String email = emailField.getText();
        String phoneNumber = phoneNumberField.getText();
        String address = addressField.getText();
        int accountId = currentUser.getAccountId();
        if ( fullName.isEmpty() || username.isEmpty() || email.isEmpty() || dob.isEmpty()) {
            return;
        }

        // Gọi phương thức cập nhật thông tin tài khoản trong DAO
        AccountDAO accountDAO = new AccountDAO();
        boolean isUpdated = accountDAO.updateAccount(accountId, fullName, username, dob, email, phoneNumber, address);

        // Hiển thị thông báo kết quả
        if (isUpdated) {
            showNotification("Profile information updated successfully!", "#2ECC71"); // Màu xanh lá
        } else {
            showNotification("Profile information updated fail!", "#E74C3C"); // Màu xanh lá

        }
    }



    @FXML
    void handleChangePasswordAction(ActionEvent event) {
        String currentPassword = currentPasswordField.getText();
        String newPassword = newPasswordField.getText();
        String confirmPassword = confirmPasswordField.getText();
        int currentAccountId = currentUser.getAccountId();
        // Kiểm tra nếu mật khẩu xác nhận không khớp với mật khẩu mới
        if (!newPassword.equals(confirmPassword)) {
            showNotification("Mật khẩu xác nhận không khớp với mật khẩu mới!", "#E74C3C");
            return; // Thoát phương thức nếu có lỗi
        }

        // Gọi phương thức để cập nhật mật khẩu
        if (accountDAO.changePassword(currentAccountId, currentPassword, newPassword)) {
            showNotification("Đổi mật khẩu thành công!", "#2ECC71");
        } else {
            showNotification("Đổi mật khẩu thất bại. Vui lòng kiểm tra lại mật khẩu hiện tại.", "#E74C3C");
        }
    }

}
