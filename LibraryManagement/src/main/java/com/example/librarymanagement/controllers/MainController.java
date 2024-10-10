package com.example.librarymanagement.controllers;

import com.example.librarymanagement.models.Account;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.Parent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import com.example.librarymanagement.dao.AccountDAO;

import java.io.ByteArrayInputStream;

public class MainController {

    @FXML
    private BorderPane borderPane; // Thay đổi: khai báo BorderPane từ FXML
    @FXML
    private ImageView profileImageView;
    @FXML
    private Label usernameLabel;
    @FXML
    private Pane userInfoPane;
    @FXML
    private VBox sidebar;
    @FXML
    private ToolBar navbar;
    @FXML
    private Button dashBoardButton;
    @FXML
    private Button booksButton;
    @FXML
    private Button readerButton;
    @FXML
    private Button borrowCardButton;
    @FXML
    private Button borrowBookButton;
    @FXML
    private Button returnBookButton;
    @FXML
    private Button invoiceButton;
    @FXML
    private Button accountButton;
    @FXML
    private Button profileButton;
    @FXML
    private Button logoutButton;
    @FXML
    private Label mainContent;
    @FXML
    private Button menuButton; // Khai báo nút menu từ FXML
    private AccountDAO accountDao = new AccountDAO();
    private int currentAccountId;

    public void setCurrentAccountId(int accountId) {
        this.currentAccountId = accountId; // Lưu ID tài khoản
        displayProfileImage(currentAccountId); // Hiển thị ảnh đại diện
    }

    private boolean isSidebarExpanded = false;
    public void setUsername(String username) {
        usernameLabel.setText(username.toUpperCase());
    }
    @FXML
    public void initialize() {
        setupButtonActions(); // Thiết lập hành động cho các nút
        AnchorPane.setLeftAnchor(navbar, sidebar.getPrefWidth());
        AnchorPane.setLeftAnchor(mainContent, sidebar.getPrefWidth());
        menuButton.setOnAction(event -> toggleSidebar());
    }

    private void setupButtonActions() {
        dashBoardButton.setOnAction(event -> {
            loadPage("DashBoard.fxml", "",  "");
            setActiveSidebarButton(dashBoardButton);
        });
        booksButton.setOnAction(event -> {
            loadPage("Book.fxml", "",  "");
            setActiveSidebarButton(booksButton);
        });
        readerButton.setOnAction(event -> {
            loadPage("Reader.fxml", "",  "");
            setActiveSidebarButton(readerButton);
        });
        borrowCardButton.setOnAction(event -> {
            loadPage("BorrowCard.fxml", "",  "");
            setActiveSidebarButton(borrowCardButton);
        });
        borrowBookButton.setOnAction(event -> {
            loadPage("BorrowBook.fxml", "",  "");
            setActiveSidebarButton(borrowBookButton);
        });
        returnBookButton.setOnAction(event -> {
            loadPage("ReturnBook.fxml", "",  "");
            setActiveSidebarButton(returnBookButton);
        });
        invoiceButton.setOnAction(event -> {
            loadPage("Invoice.fxml", "",  "");
            setActiveSidebarButton(invoiceButton);
        });
        accountButton.setOnAction(event -> {
            loadPage("Account.fxml", "",  "");
            setActiveSidebarButton(accountButton);
        });
        profileButton.setOnAction(event -> {
            loadPage("Profile.fxml", "",  "");
            setActiveSidebarButton(profileButton);
        });
        logoutButton.setOnAction(event -> {
            handleLogout();
            setActiveSidebarButton(logoutButton);
        });
    }


    public void loadPage(String fxmlFile, String notificationMessage, String notificationColor) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/librarymanagement/fxml/" + fxmlFile));
            Parent newPage = loader.load();

            if ("Profile.fxml".equals(fxmlFile)) {
                ProfileController profileController = loader.getController();
                profileController.loadProfile(currentAccountId); // Gọi phương thức để tải thông tin tài khoản
            } else if ("Reader.fxml".equals(fxmlFile)) {
                ReaderController readerController = loader.getController();
                readerController.setMainController(this); // Thiết lập MainController cho ReaderController
                if (notificationMessage != null && notificationColor != null) {
                    readerController.showNotification(notificationMessage, notificationColor);
                }
            }

            borderPane.setCenter(newPage); // Chuyển nội dung của BorderPane
        } catch (Exception e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR, "Lỗi khi tải trang: " + e.getMessage());
            alert.showAndWait();
        }
    }


    public void loadDashboardView() {
        try {
            // Tải dashboard từ tệp FXML
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/librarymanagement/fxml/Dashboard.fxml"));
            Parent dashboardView = loader.load();

            // Hiển thị dashboard trong BorderPane (vị trí center)
            borderPane.setCenter(dashboardView);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void displayProfileImage(int accountId) {
        AccountDAO accountDAO = new AccountDAO();
        Account account = accountDAO.getAccountById(accountId); // Lấy thông tin tài khoản

        if (account != null) {
            byte[] imageBytes = account.getProfileImage();

            if (imageBytes != null) {
                try {
                    // Chuyển đổi byte[] thành Image
                    ByteArrayInputStream inputStream = new ByteArrayInputStream(imageBytes);
                    Image image = new Image(inputStream);

                    // Thiết lập Image cho ImageView
                    profileImageView.setImage(image);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }


    private void handleLogout() {
        Alert confirmLogoutAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmLogoutAlert.setTitle("Xác nhận đăng xuất");
        confirmLogoutAlert.setHeaderText(null);
        confirmLogoutAlert.setContentText("Bạn có chắc chắn muốn đăng xuất không?");

        // Hiển thị hộp thoại và chờ người dùng phản hồi
        confirmLogoutAlert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                // Nếu người dùng xác nhận, thực hiện đăng xuất và chuyển về trang đăng nhập
                performLogout();
            }
        });
    }

    private void performLogout() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/librarymanagement/fxml/Login.fxml"));
            Parent loginPage = loader.load();

            Stage stage = (Stage) borderPane.getScene().getWindow();

            // Tạo scene mới cho trang đăng nhập
            Scene loginScene = new Scene(loginPage);

            // Chuyển sang màn hình đăng nhập
            stage.setScene(loginScene);
            stage.setTitle("Login");
            stage.centerOnScreen();
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR, "Lỗi khi tải trang đăng nhập: " + e.getMessage());
            alert.showAndWait();
        }
    }



    private void toggleSidebar() {
        if (isSidebarExpanded) {
            // Thu nhỏ sidebar
            sidebar.setPrefWidth(50);
            AnchorPane.setLeftAnchor(mainContent, 50.0);
            userInfoPane.setVisible(false);
            usernameLabel.setVisible(false);
        } else {
            // Mở rộng sidebar
            sidebar.setPrefWidth(180);
            AnchorPane.setLeftAnchor(mainContent, 180.0);
            userInfoPane.setVisible(true);
            usernameLabel.setVisible(true);
        }
        isSidebarExpanded = !isSidebarExpanded;
    }

    private void setActiveSidebarButton(Button activeButton) {
        // Xóa lớp 'active' khỏi tất cả các nút sidebar
        dashBoardButton.getStyleClass().remove("active");
        booksButton.getStyleClass().remove("active");
        readerButton.getStyleClass().remove("active");
        borrowCardButton.getStyleClass().remove("active");
        borrowBookButton.getStyleClass().remove("active");
        returnBookButton.getStyleClass().remove("active");
        invoiceButton.getStyleClass().remove("active");
        accountButton.getStyleClass().remove("active");
        profileButton.getStyleClass().remove("active");
        logoutButton.getStyleClass().remove("active");

        // Thêm lớp 'active' vào nút hiện tại
        activeButton.getStyleClass().add("active");
    }

}
