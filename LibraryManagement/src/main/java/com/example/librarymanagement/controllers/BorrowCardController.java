package com.example.librarymanagement.controllers;

import com.example.librarymanagement.dao.BorrowCardDAO;
import com.example.librarymanagement.dao.ReaderDAO;
import com.example.librarymanagement.models.Book;
import com.example.librarymanagement.models.BorrowCard;
import com.example.librarymanagement.models.Reader;
import com.example.librarymanagement.request.BorrowCardRequest;
import com.example.librarymanagement.request.ReaderRequest;
import javafx.animation.FadeTransition;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.util.Callback;
import javafx.util.Duration;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.Period;
import java.util.List;
import java.util.Locale;

public class BorrowCardController {
    private MainController mainController;
    @FXML
    private TableView<BorrowCard> borrowCardTable;
    private static final int ROWS_PER_PAGE = 18;
    @FXML
    private Pagination pagination;
    @FXML
    private TableColumn<BorrowCard, Long> idColumn;
    @FXML
    private TableColumn<BorrowCard, String> cardCodeColumn;
    @FXML
    private TableColumn<BorrowCard, String> fullNameColumn;
    @FXML
    private TableColumn<BorrowCard, String> genderColumn;
    @FXML
    private TableColumn<BorrowCard, String> dobColumn;
    @FXML
    private TableColumn<BorrowCard, String> emailColumn;
    @FXML
    private TableColumn<BorrowCard, String> phoneNumberColumn;
    @FXML
    private TableColumn<BorrowCard, String> addressColumn;
    @FXML
    private TableColumn<BorrowCard, Integer> depositColumn;
    @FXML
    private TextField idField;
    @FXML
    private TextField fullNameField;
    @FXML
    private ToggleGroup genderToggleGroup;
    @FXML
    private DatePicker dobField;
    @FXML
    private TextField emailField;
    @FXML
    private TextField phoneNumberField;
    @FXML
    private TextField addressField;
    @FXML
    private TextField searchField;
    @FXML
    private VBox notificationBox;
    @FXML
    private Label notificationLabel;

    @FXML
    private Button updateButton;

    BorrowCardDAO borrowCardDAO = new BorrowCardDAO();
    private ObservableList<BorrowCard> borrowCardList = FXCollections.observableArrayList();

    public void setMainController(MainController controller) {
        this.mainController = controller;
    }
    @FXML
    public void initialize() {
        loadBorrowCardData();
        setupPagination();
        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            filterCards(newValue); // Gọi phương thức lọc khi có sự thay đổi
        });
        depositColumn.setCellFactory(new Callback<TableColumn<BorrowCard, Integer>, TableCell<BorrowCard, Integer>>() {
            @Override
            public TableCell<BorrowCard, Integer> call(TableColumn<BorrowCard, Integer> column) {
                return new TableCell<BorrowCard, Integer>() {
                    @Override
                    protected void updateItem(Integer deposit, boolean empty) {
                        super.updateItem(deposit, empty);
                        if (empty || deposit == null) {
                            setText(null);
                        } else {
                            // Định dạng giá với dấu chấm và thêm 'vnđ'
                            NumberFormat formatter = NumberFormat.getNumberInstance(new Locale("vi", "VN"));
                            setText(formatter.format(deposit) + "đ");
                        }
                    }
                };
            }
        });
        borrowCardTable.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                populateFieldsFromSelectedCard(newValue);
            }
        });
    }

    private void populateFieldsFromSelectedCard(BorrowCard selectedCard) {
        idField.setText(String.valueOf(selectedCard.getBorrowCardId()));
        fullNameField.setText(selectedCard.getFullName());
        emailField.setText(selectedCard.getEmail());
        phoneNumberField.setText(selectedCard.getPhoneNumber());
        addressField.setText(selectedCard.getAddress());
        dobField.setValue(selectedCard.getDob());

        // Thiết lập giới tính dựa trên thông tin độc giả đã chọn
        String gender = selectedCard.getGender();
        for (Toggle toggle : genderToggleGroup.getToggles()) {
            RadioButton radioButton = (RadioButton) toggle;
            if (radioButton.getText().equals(gender)) {
                genderToggleGroup.selectToggle(radioButton);
                break;
            }
        }
    }

    private void setupPagination() {
        int totalPageCount = (int) Math.ceil((double) borrowCardList.size() / ROWS_PER_PAGE);
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
        int toIndex = Math.min(fromIndex + ROWS_PER_PAGE, borrowCardList.size());

        borrowCardTable.setItems(FXCollections.observableArrayList(borrowCardList.subList(fromIndex, toIndex)));
    }

    private void loadBorrowCardData() {
        List<BorrowCard> borrowCards = borrowCardDAO.getAllBorrowCards();
        borrowCards.sort((i1, i2) -> i2.getBorrowCardId().compareTo(i1.getBorrowCardId()));
        borrowCardList.setAll(borrowCards);

        // Đặt giá trị cho các cột
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        cardCodeColumn.setCellValueFactory(new PropertyValueFactory<>("cardCode"));
        fullNameColumn.setCellValueFactory(new PropertyValueFactory<>("fullName"));
        genderColumn.setCellValueFactory(new PropertyValueFactory<>("gender"));
        dobColumn.setCellValueFactory(new PropertyValueFactory<>("dob"));
        emailColumn.setCellValueFactory(new PropertyValueFactory<>("email"));
        phoneNumberColumn.setCellValueFactory(new PropertyValueFactory<>("phoneNumber"));
        addressColumn.setCellValueFactory(new PropertyValueFactory<>("address"));
        depositColumn.setCellValueFactory(new PropertyValueFactory<>("deposit"));
        // Đặt dữ liệu vào TableView
        borrowCardTable.setItems(borrowCardList);
        setupPagination();

    }

    private void filterCards(String keyword) {
        ObservableList<BorrowCard> filteredList = FXCollections.observableArrayList();

        // Lọc danh sách tài khoản
        for (BorrowCard borrowCard : borrowCardList) {
            if (borrowCard.getCardCode().toLowerCase().contains(keyword.toLowerCase()) ||
                    borrowCard.getFullName().toLowerCase().contains(keyword.toLowerCase()) ||
                    borrowCard.getEmail().toLowerCase().contains(keyword.toLowerCase()) ||
                    borrowCard.getPhoneNumber().toLowerCase().contains(keyword.toLowerCase())||
                    borrowCard.getAddress().toLowerCase().contains(keyword.toLowerCase())) {
                filteredList.add(borrowCard);
            }
        }

        // Cập nhật TableView với danh sách đã lọc
        borrowCardTable.setItems(filteredList);
    }

    public void addBorrowCard(ActionEvent event) {
        // Lấy mã độc giả cuối cùng từ cơ sở dữ liệu
        String lastBorrowCardCode = borrowCardDAO.getLastCardCode();

        // Tạo mã độc giả mới
        String newCardCode = generateNextBorrowCardCode(lastBorrowCardCode);

        // Lấy thông tin từ các trường nhập liệu
        String fullName = fullNameField.getText();
        String email = emailField.getText();
        String phoneNumber = phoneNumberField.getText();
        String address = addressField.getText();
        LocalDate dob = dobField.getValue();

        // Kiểm tra RadioButton và lấy giới tính
        String gender = "";
        if (genderToggleGroup.getSelectedToggle() != null) {
            gender = ((RadioButton) genderToggleGroup.getSelectedToggle()).getText();
        } else {
            showNotification("Please select a gender!", "#E74C3C"); // Màu đỏ
            return; // Ngưng thực hiện nếu không chọn giới tính
        }

        // Tạo đối tượng ReaderRequest
        BorrowCardRequest borrowCardRequest = new BorrowCardRequest(fullName, email, phoneNumber, address, dob, gender, newCardCode);

        // Thêm độc giả vào cơ sở dữ liệu
        try {
            borrowCardDAO.addBorrowCard(borrowCardRequest);
            mainController.loadPage("BorrowCard.fxml", "Borrow Card updated successfully!",  "#2ECC71");
            loadBorrowCardData();
            showNotification("Create borrow card successfully!", "#2ECC71"); // Màu xanh lá
            if (dob != null && Period.between(dob, LocalDate.now()).getYears() > 18) {
                // Hiển thị alert nếu độc giả trên 18 tuổi
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Payment Required");
                alert.setHeaderText("Payment Required");
                alert.setContentText("Added book borrowing card successfully. Please proceed to payment.");

                ButtonType invoiceButton = new ButtonType("See Invoice");
                alert.getButtonTypes().setAll(invoiceButton, ButtonType.CANCEL);

                // Hiển thị alert và xử lý phản hồi
                alert.showAndWait().ifPresent(response -> {
                    if (response == invoiceButton) {
                        mainController.loadPage("Invoice.fxml", "",  ""); // Chuyển đến trang hóa đơn
                    }
                });
            }
            handleRefresh(); // Làm mới bảng sau khi thêm
        } catch (Exception e) {
            showNotification("Failed to create borrow card to the database! Please try again.", "#E74C3C"); // Màu đỏ
            e.printStackTrace();
        }
    }
    public void updateBorrowCard(ActionEvent event) {
        String idText = idField.getText();
        if (idText.isEmpty()) {
            showNotification("Please select a borrow card to update!", "#E74C3C"); // Màu đỏ
            return; // Ngưng thực hiện nếu không có ID
        }

        Long readerId = Long.parseLong(idText);

        String fullName = fullNameField.getText();
        String email = emailField.getText();
        String phoneNumber = phoneNumberField.getText();
        String address = addressField.getText();
        LocalDate dob = dobField.getValue();

        // Kiểm tra RadioButton và lấy giới tính
        String gender = "";
        if (genderToggleGroup.getSelectedToggle() != null) {
            gender = ((RadioButton) genderToggleGroup.getSelectedToggle()).getText();
        } else {
            showNotification("Please select a gender!", "#FF9224"); // Màu đỏ
            return; // Ngưng thực hiện nếu không chọn giới tính
        }

        // Tạo đối tượng ReaderRequest cho việc cập nhật
        BorrowCardRequest borrowCardRequest = new BorrowCardRequest(fullName, email, phoneNumber, address, dob, gender, null);
        borrowCardRequest.setId(readerId); // Gán ID cho đối tượng ReaderRequest

        // Cập nhật thông tin độc giả trong cơ sở dữ liệu
        try {
            borrowCardDAO.updateBorrowCard(borrowCardRequest);
            mainController.loadPage("BorrowCard.fxml", "Borrow card updated successfully!",  "#2ECC71");
            setupPagination();
            updateTableView(pagination.getCurrentPageIndex());
        } catch (Exception e) {
            showNotification("Failed to update borrow card in the database! Please try again.", "#E74C3C"); // Màu đỏ
            e.printStackTrace();
        }
    }
    public void deleteBorrowCard(ActionEvent event) {
        // Lấy sách đang chọn từ bảng
        BorrowCard selectedBorrowCard = borrowCardTable.getSelectionModel().getSelectedItem();

        if (selectedBorrowCard != null) {
            // Hiển thị hộp thoại xác nhận
            Alert confirmationAlert = new Alert(Alert.AlertType.CONFIRMATION);
            confirmationAlert.setTitle("Confirm Deletion");
            confirmationAlert.setHeaderText("Are you sure you want to delete this reader?");
            confirmationAlert.setContentText(selectedBorrowCard.getFullName());

            confirmationAlert.showAndWait().ifPresent(response -> {
                if (response == ButtonType.OK) {
                    try {
                        borrowCardDAO.deleteBorrowCard(selectedBorrowCard.getBorrowCardId()); // Gọi phương thức xóa

                        // Xóa tài khoản khỏi TableView
                        borrowCardTable.getItems().remove(selectedBorrowCard);
                        showNotification("Delete borrow card successfully!", "#2ECC71"); // Màu xanh lá
                        handleRefresh();
                    } catch (Exception e) {
                        Alert errorAlert = new Alert(Alert.AlertType.ERROR);
                        errorAlert.setTitle("Error Deleting borrow card");
                        errorAlert.setHeaderText(null);
                        errorAlert.setContentText("An error occurred while deleting the borrow card: " + e.getMessage());
                        errorAlert.showAndWait();
                    }
                }
            });
        } else {
            // Hiển thị thông báo nếu không có tài khoản nào được chọn
            showNotification("Please choose a borrow card to delete!", "#FF9224"); // Màu xanh lá

        }
    }

    private String generateNextBorrowCardCode(String lastReaderCode) {
        if (lastReaderCode == null || lastReaderCode.isEmpty()) {
            return "BRC0001";  // Nếu không có sách nào trong DB
        }

        // Tách phần số ra khỏi mã sách (Ví dụ: BOOK012 -> 12)
        int currentCode = Integer.parseInt(lastReaderCode.substring(3));
        int newCode = currentCode + 1;  // Tăng số lên

        // Sinh mã mới với định dạng BOOKxxx
        return "BRC" + String.format("%04d", newCode);
    }

    @FXML
    private void handleRefresh() {
        clearFields();
    }

    private void clearFields() {

        genderToggleGroup.getToggles().clear();
        fullNameField.clear();
        genderToggleGroup.selectToggle(null);
        dobField.setValue(null);
        emailField.clear();
        phoneNumberField.clear();
        addressField.clear();
    }



    public void handleExportBorrwCards(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV Files", "*.csv"));
        File file = fileChooser.showSaveDialog(null);

        if (file != null) {
            exportCardsToCSV(borrowCardList, file.getAbsolutePath());
        }
    }
    public void exportCardsToCSV(List<BorrowCard> borrowCardList, String filePath) {
        try (FileWriter writer = new FileWriter(filePath)) {
            // Viết tiêu đề cho file CSV
            writer.append("Borrow Card Code,Full Name,Gender,Date of Birth,Email,Phone Number,Address,Deposit\n");

            // Viết thông tin sách
            for (BorrowCard borrowCard : borrowCardList) {
                writer.append(borrowCard.getCardCode())
                        .append(",")
                        .append(borrowCard.getFullName())
                        .append(",")
                        .append(borrowCard.getGender())
                        .append(",")
                        .append(borrowCard.getDob().toString())
                        .append(",")
                        .append(borrowCard.getEmail())
                        .append(",")
                        .append(borrowCard.getPhoneNumber())
                        .append(",")
                        .append(borrowCard.getAddress())
                        .append(",")
                        .append(borrowCard.getDeposit().toString())
                        .append("\n");
            }
            System.out.println("Export successful: " + filePath);
            showNotification("Export successful: " + filePath, "#2ECC71"); // Màu xanh lá
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Export failed: " + e.getMessage());
            showNotification("Export failed: " + filePath, "red"); // Màu xanh lá

        }
    }
    public void showNotification(String message, String color) {
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
}
