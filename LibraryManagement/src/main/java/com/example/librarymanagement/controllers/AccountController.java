package com.example.librarymanagement.controllers;

import com.example.librarymanagement.dao.AccountDAO;
import com.example.librarymanagement.models.Account;
import com.example.librarymanagement.request.AccountRequest;
import com.example.librarymanagement.respone.AccountResponse;
import javafx.animation.FadeTransition;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Rectangle;
import javafx.stage.FileChooser;
import javafx.util.Duration;
import javafx.scene.control.cell.PropertyValueFactory;

import org.mindrot.jbcrypt.BCrypt;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;

public class AccountController {
    @FXML
    private TableView<AccountResponse> accountsTable;
    private static final int ROWS_PER_PAGE = 18;
    @FXML
    private Pagination pagination;
    @FXML
    private TableColumn<AccountResponse, Integer> idColumn;
    @FXML
    private TableColumn<AccountResponse, String> fullNameColumn;

    @FXML
    private TableColumn<AccountResponse, String> usernameColumn;

    @FXML
    private TableColumn<AccountResponse, String> dobColumn;

    @FXML
    private TableColumn<AccountResponse, String> emailColumn;

    @FXML
    private TableColumn<AccountResponse, String> phoneNumberColumn;

    @FXML
    private TableColumn<AccountResponse, String> addressColumn;
    @FXML
    private VBox notificationBox;
    @FXML
    private Label notificationLabel;
    @FXML
    private TextField searchField;
    @FXML
    private TextField idField;
    @FXML
    private TextField fullNameField; // Trường nhập tên đầy đủ
    @FXML
    private TextField usernameField; // Trường nhập tên người dùng
    @FXML
    private TextField dobField; // Trường nhập ngày sinh
    @FXML
    private TextField emailField; // Trường nhập email
    @FXML
    private TextField phoneNumberField; // Trường nhập số điện thoại
    @FXML
    private TextField addressField; // Trường nhập địa chỉ
    @FXML
    private TextField passwordField; // Trường nhập mật khẩu
    @FXML
    private ImageView userImage;
    @FXML
    private Pane imagePane; // Pane chứa ImageView và vùng chọn
    @FXML
    private ImageView croppedImagePreview; // Để hiển thị ảnh đã cắt

    private Rectangle cropArea;
    private double startX, startY; // Điểm bắt đầu kéo hình vuông
    private Image originalImage;
    private byte[] avatar;
    private ObservableList<AccountResponse> accountsList;
    private AccountDAO accountDAO = new AccountDAO();

    @FXML
    public void initialize() {
        accountsList = FXCollections.observableArrayList();
        loadAccounts();
        setupPagination();

        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            filterAccounts(newValue); // Gọi phương thức lọc khi có sự thay đổi
        });

        accountsTable.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                // Kiểm tra nếu click vào một dòng
                if (accountsTable.getSelectionModel().getSelectedItem() != null) {
                    AccountResponse selectedAccount = accountsTable.getSelectionModel().getSelectedItem();
                    idField.setText(String.valueOf(selectedAccount.getId()));
                    fullNameField.setText(selectedAccount.getFullName());
                    usernameField.setText(selectedAccount.getUsername());
                    dobField.setText(selectedAccount.getDateOfBirth());
                    emailField.setText(selectedAccount.getEmail());
                    phoneNumberField.setText(selectedAccount.getPhoneNumber());
                    addressField.setText(selectedAccount.getAddress());
                    // Bạn có thể không hiển thị mật khẩu hoặc xử lý nó theo cách an toàn
                }
            }
        });
    }

    private void setupPagination() {
        int totalPageCount = (int) Math.ceil((double) accountsList.size() / ROWS_PER_PAGE);
        pagination.setPageCount(totalPageCount);

        pagination.currentPageIndexProperty().addListener((observable, oldValue, newValue) -> {
            updateTableView(newValue.intValue()); // Cập nhật dữ liệu cho TableView khi thay đổi trang
        });

        // Hiển thị dữ liệu trang đầu tiên
        updateTableView(0);
    }

    // Cập nhật dữ liệu cho TableView dựa trên trang hiện tại
    private void updateTableView(int pageIndex) {
        int fromIndex = pageIndex * ROWS_PER_PAGE;
        int toIndex = Math.min(fromIndex + ROWS_PER_PAGE, accountsList.size());

        accountsTable.setItems(FXCollections.observableArrayList(accountsList.subList(fromIndex, toIndex)));
    }
    private void loadAccounts() {
        // Lấy danh sách tài khoản từ DAO
        accountsList = FXCollections.observableArrayList(accountDAO.getAllAccounts());

        // Thiết lập các cột trong TableView
        idColumn.setCellValueFactory(new PropertyValueFactory<AccountResponse, Integer>("id"));
        fullNameColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getFullName()));
        usernameColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getUsername()));
        dobColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getDateOfBirth()));
        emailColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getEmail()));
        phoneNumberColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getPhoneNumber()));
        addressColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getAddress()));

        // Cập nhật TableView với danh sách tài khoản
        accountsTable.setItems(accountsList);
        setupPagination();
    }


    @FXML
    private void handleImageUpload() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.gif"));

        File selectedFile = fileChooser.showOpenDialog(userImage.getScene().getWindow());
        if (selectedFile != null) {
            try (InputStream inputStream = new FileInputStream(selectedFile)) {
                avatar = new byte[(int) selectedFile.length()];
                inputStream.read(avatar); // Đọc file vào mảng byte
            } catch (IOException e) {
                e.printStackTrace();
                Alert alert = new Alert(Alert.AlertType.ERROR, "Không thể tải ảnh. Vui lòng thử lại.");
                alert.showAndWait();
            }

            // Tạo hình ảnh từ tệp được chọn
            Image image = new Image(selectedFile.toURI().toString());
            userImage.setImage(image);
        }
    }

    @FXML
    private void addAccount() {
        // Kiểm tra các trường nhập liệu có được điền hay không
        if (isInputValid()) {
            // Tạo một đối tượng AccountRequest từ dữ liệu nhập vào
            AccountRequest accountRequest = new AccountRequest();
            accountRequest.setAvatar(avatar);
            accountRequest.setFullName(fullNameField.getText());
            accountRequest.setUsername(usernameField.getText());
            accountRequest.setDateOfBirth(dobField.getText());
            accountRequest.setEmail(emailField.getText());
            accountRequest.setPhoneNumber(phoneNumberField.getText());
            accountRequest.setAddress(addressField.getText());
            String hashedPassword = BCrypt.hashpw(passwordField.getText(), BCrypt.gensalt());
            accountRequest.setPassword(hashedPassword);

            // Gọi phương thức kiểm tra trùng lặp từ DAO
            if (accountDAO.isUsernameOrEmailTaken(accountRequest.getUsername(), accountRequest.getEmail(), accountRequest.getPhoneNumber())) {
                showNotification("Username, Email or Phone Number already exists!", "#E67E22"); // Màu cam
                return;
            }

            // Gọi phương thức thêm tài khoản từ DAO
            boolean success = accountDAO.addAccount(accountRequest);
            if (success) {
                showNotification("Add new account successfully!", "#2ECC71"); // Màu xanh lá
                handleRefresh();

                // Thêm tài khoản mới vào danh sách và đặt nó ở đầu danh sách
                AccountResponse newAccount = new AccountResponse(); // Tạo đối tượng AccountResponse từ dữ liệu vừa thêm
                newAccount.setId(newAccount.getId()); // Lấy ID của tài khoản mới nhất vừa thêm
                newAccount.setFullName(accountRequest.getFullName());
                newAccount.setUsername(accountRequest.getUsername());
                newAccount.setDateOfBirth(accountRequest.getDateOfBirth());
                newAccount.setEmail(accountRequest.getEmail());
                newAccount.setPhoneNumber(accountRequest.getPhoneNumber());
                newAccount.setAddress(accountRequest.getAddress());
                // Thêm tài khoản mới vào đầu danh sách
                accountsList.add(0, newAccount);

                // Cập nhật lại TableView và đặt lại trang hiện tại về 0
                setupPagination(); // Cập nhật số trang
                pagination.setCurrentPageIndex(0); // Quay lại trang đầu tiên
                updateTableView(0); // Cập nhật TableView để hiển thị tài khoản mới
            } else {
                showNotification("Add new account failed!", "#E74C3C"); // Màu đỏ
            }
        }
    }


    @FXML
    private void handleRefresh() {
        // Reset tất cả các trường TextField về giá trị rỗng
        fullNameField.clear();
        usernameField.clear();
        dobField.clear();
        emailField.clear();
        phoneNumberField.clear();
        addressField.clear();
        passwordField.clear();

        // Đặt lại hình ảnh về hình mặc định hoặc xóa ảnh
        userImage.setImage(null); // Hoặc set hình mặc định: userImage.setImage(new Image("path/to/default/image.png"));

        // Reset giá trị avatar (nếu có)
        avatar = null;
    }


    private boolean isInputValid() {
        String errorMessage = "";

        if (fullNameField.getText() == null || fullNameField.getText().isEmpty()) {
            errorMessage += "Tên đầy đủ không được để trống!\n";
        }
        if (usernameField.getText() == null || usernameField.getText().isEmpty()) {
            errorMessage += "Tên người dùng không được để trống!\n";
        }
        if (dobField.getText() == null || dobField.getText().isEmpty()) {
            errorMessage += "Ngày sinh không được để trống!\n";
        }
        if (emailField.getText() == null || emailField.getText().isEmpty()) {
            errorMessage += "Email không được để trống!\n";
        }
        if (phoneNumberField.getText() == null || phoneNumberField.getText().isEmpty()) {
            errorMessage += "Số điện thoại không được để trống!\n";
        }
        if (addressField.getText() == null || addressField.getText().isEmpty()) {
            errorMessage += "Địa chỉ không được để trống!\n";
        }
        if (passwordField.getText() == null || passwordField.getText().isEmpty()) {
            errorMessage += "Mật khẩu không được để trống!\n";
        }

        if (errorMessage.isEmpty()) {
            return true; // Tất cả các trường hợp lệ
        } else {
            showNotification(errorMessage, "#E67E22");
            return false; // Có trường không hợp lệ
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

    @FXML
    public void deleteAccount() {
        // Lấy tài khoản đã chọn
        AccountResponse selectedAccount = accountsTable.getSelectionModel().getSelectedItem();

        // Kiểm tra nếu có tài khoản nào được chọn
        if (selectedAccount != null) {
            // Hiển thị hộp thoại xác nhận
            Alert confirmationAlert = new Alert(Alert.AlertType.CONFIRMATION);
            confirmationAlert.setTitle("Confirm Deletion");
            confirmationAlert.setHeaderText("Are you sure you want to delete this account?");
            confirmationAlert.setContentText(selectedAccount.getFullName());

            confirmationAlert.showAndWait().ifPresent(response -> {
                if (response == ButtonType.OK) {
                    try {
                        // Gọi phương thức xóa tài khoản từ cơ sở dữ liệu
                        AccountDAO accountDAO = new AccountDAO(); // Khởi tạo AccountDAO với kết nối
                        accountDAO.deleteAccount(selectedAccount.getId()); // Gọi phương thức xóa

                        // Xóa tài khoản khỏi TableView
                        accountsTable.getItems().remove(selectedAccount);
                        showNotification("Delete account successfully!", "#2ECC71"); // Màu xanh lá
                        handleRefresh();
                    } catch (SQLException e) {
                        // Hiển thị thông báo lỗi nếu xảy ra vấn đề
                        showNotification("Delete account failed!", "#E74C3C"); // Màu xanh lá
                    }
                }
            });
        } else {
            showNotification("Please choose account to delete!", "#E67E22"); // Màu xanh lá

        }
    }

    private void filterAccounts(String keyword) {
        ObservableList<AccountResponse> filteredList = FXCollections.observableArrayList();

        // Lọc danh sách tài khoản
        for (AccountResponse account : accountsList) {
            if (account.getFullName().toLowerCase().contains(keyword.toLowerCase()) ||
                    account.getUsername().toLowerCase().contains(keyword.toLowerCase()) ||
                    account.getEmail().toLowerCase().contains(keyword.toLowerCase()) ||
                    account.getPhoneNumber().contains(keyword.toLowerCase())) {
                filteredList.add(account);
            }
        }

        // Cập nhật TableView với danh sách đã lọc
        accountsTable.setItems(filteredList);
    }


}
