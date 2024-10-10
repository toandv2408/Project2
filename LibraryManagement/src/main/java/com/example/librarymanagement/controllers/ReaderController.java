package com.example.librarymanagement.controllers;

import com.example.librarymanagement.dao.BookDAO;
import com.example.librarymanagement.dao.ReaderDAO;
import com.example.librarymanagement.models.Book;
import com.example.librarymanagement.models.Reader;
import com.example.librarymanagement.request.BookRequest;
import com.example.librarymanagement.request.ReaderRequest;
import javafx.animation.FadeTransition;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDate;
import java.time.Period;
import java.util.List;

public class ReaderController {
    @FXML
    private Button invoiceButton; // Nút để chuyển đến Invoice

    private MainController mainController;
    @FXML
    private TableView<Reader> readerTable;
    @FXML
    private TableColumn<Reader, Long> idColumn;
    @FXML
    private TableColumn<Reader, String> readerCodeColumn;
    @FXML
    private TableColumn<Reader, String> fullNameColumn;
    @FXML
    private TableColumn<Reader, String> genderColumn;
    @FXML
    private TableColumn<Reader, String> dobColumn;
    @FXML
    private TableColumn<Reader, String> emailColumn;
    @FXML
    private TableColumn<Reader, String> phoneNumberColumn;
    @FXML
    private TableColumn<Reader, String> citizenshipCardColumn;
    @FXML
    private TableColumn<Reader, String> addressColumn;
    @FXML
    private TableColumn<Reader, String> expiryDateColumn;

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
    private TextField citizenshipCardField;
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

    ReaderDAO readerDAO = new ReaderDAO();
    private ObservableList<Reader> readerList = FXCollections.observableArrayList();

    public void setMainController(MainController controller) {
        this.mainController = controller;
    }


    @FXML
    public void initialize() {
        loadReaderData();
        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            filterReaders(newValue); // Gọi phương thức lọc khi có sự thay đổi
        });
        readerTable.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                populateFieldsFromSelectedReader(newValue);
            }
        });
    }

    private void loadReaderData() {
        List<Reader> reders = readerDAO.getAllReaders();

        readerList.setAll(reders);

        // Đặt giá trị cho các cột
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        readerCodeColumn.setCellValueFactory(new PropertyValueFactory<>("readerCode"));
        fullNameColumn.setCellValueFactory(new PropertyValueFactory<>("fullName"));
        genderColumn.setCellValueFactory(new PropertyValueFactory<>("gender"));
        dobColumn.setCellValueFactory(new PropertyValueFactory<>("dob"));
        emailColumn.setCellValueFactory(new PropertyValueFactory<>("email"));
        phoneNumberColumn.setCellValueFactory(new PropertyValueFactory<>("phoneNumber"));
        citizenshipCardColumn.setCellValueFactory(new PropertyValueFactory<>("citizenshipCard"));
        addressColumn.setCellValueFactory(new PropertyValueFactory<>("address"));
        expiryDateColumn.setCellValueFactory(new PropertyValueFactory<>("expiryDate"));

        // Đặt dữ liệu vào TableView
        readerTable.setItems(readerList);
    }

    private void populateFieldsFromSelectedReader(Reader selectedReader) {
        idField.setText(String.valueOf(selectedReader.getReaderId()));
        fullNameField.setText(selectedReader.getFullName());
        emailField.setText(selectedReader.getEmail());
        phoneNumberField.setText(selectedReader.getPhoneNumber());
        citizenshipCardField.setText(selectedReader.getCitizenshipCard());
        addressField.setText(selectedReader.getAddress());
        dobField.setValue(selectedReader.getDob());

        // Thiết lập giới tính dựa trên thông tin độc giả đã chọn
        String gender = selectedReader.getGender();
        for (Toggle toggle : genderToggleGroup.getToggles()) {
            RadioButton radioButton = (RadioButton) toggle;
            if (radioButton.getText().equals(gender)) {
                genderToggleGroup.selectToggle(radioButton);
                break;
            }
        }
    }

    @FXML
    private void handleExportReaders(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV Files", "*.csv"));
        File file = fileChooser.showSaveDialog(null);

        if (file != null) {
            exportReadersToCSV(readerList, file.getAbsolutePath());
        }
    }

    public void exportReadersToCSV(List<Reader> readerList, String filePath) {
        try (FileWriter writer = new FileWriter(filePath)) {
            // Viết tiêu đề cho file CSV
            writer.append("Reader,Full Name,Gender,Date of Birth,Email,Phone Number,Citizenship Card,Address\n");

            // Viết thông tin sách
            for (Reader reader : readerList) {
                writer.append(reader.getReaderCode())
                        .append(",")
                        .append(reader.getFullName())
                        .append(",")
                        .append(reader.getGender())
                        .append(",")
                        .append(reader.getDob().toString())
                        .append(",")
                        .append(reader.getEmail())
                        .append(",")
                        .append(reader.getPhoneNumber())
                        .append(",")
                        .append(reader.getCitizenshipCard())
                        .append(",")
                        .append(reader.getAddress())
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

    private void filterReaders(String keyword) {
        ObservableList<Reader> filteredList = FXCollections.observableArrayList();

        // Lọc danh sách tài khoản
        for (Reader reader : readerList) {
            if (reader.getReaderCode().toLowerCase().contains(keyword.toLowerCase()) ||
                    reader.getFullName().toLowerCase().contains(keyword.toLowerCase()) ||
                    reader.getEmail().toLowerCase().contains(keyword.toLowerCase()) ||
                    reader.getCitizenshipCard().toLowerCase().contains(keyword.toLowerCase()) ||
                    reader.getPhoneNumber().toLowerCase().contains(keyword.toLowerCase())||
                    reader.getAddress().toLowerCase().contains(keyword.toLowerCase())) {
                filteredList.add(reader);
            }
        }

        // Cập nhật TableView với danh sách đã lọc
        readerTable.setItems(filteredList);
    }

    @FXML
    private void addReader() {
        // Lấy mã độc giả cuối cùng từ cơ sở dữ liệu
        String lastReaderCode = readerDAO.getLastReaderCode();

        // Tạo mã độc giả mới
        String newReaderCode = generateNextReaderCode(lastReaderCode);

        // Lấy thông tin từ các trường nhập liệu
        String fullName = fullNameField.getText();
        String email = emailField.getText();
        String phoneNumber = phoneNumberField.getText();
        String citizenshipCard = citizenshipCardField.getText();
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
        ReaderRequest readerRequest = new ReaderRequest(fullName, email, phoneNumber, address, dob, gender, citizenshipCard, newReaderCode);

        // Thêm độc giả vào cơ sở dữ liệu
        try {
            readerDAO.addReader(readerRequest);
            loadReaderData();
            showNotification("Add reader successfully!", "#2ECC71"); // Màu xanh lá
            if (dob != null && Period.between(dob, LocalDate.now()).getYears() > 18) {
                // Hiển thị alert nếu độc giả trên 18 tuổi
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Payment Required");
                alert.setHeaderText("Payment Required");
                alert.setContentText("The reader is over 18 years old. Please proceed to payment.");

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
            showNotification("Failed to add reader to the database! Please try again.", "#E74C3C"); // Màu đỏ
            e.printStackTrace();
        }
    }


    @FXML
    private void updateReader() {
        // Lấy ID độc giả từ trường idField
        String idText = idField.getText();
        if (idText.isEmpty()) {
            showNotification("Please select a reader to update!", "#E74C3C"); // Màu đỏ
            return; // Ngưng thực hiện nếu không có ID
        }

        Long readerId = Long.parseLong(idText);

        // Lấy thông tin từ các trường nhập liệu
        String fullName = fullNameField.getText();
        String email = emailField.getText();
        String phoneNumber = phoneNumberField.getText();
        String citizenshipCard = citizenshipCardField.getText();
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
        ReaderRequest readerRequest = new ReaderRequest(fullName, email, phoneNumber, address, dob, gender, citizenshipCard, null);
        readerRequest.setId(readerId); // Gán ID cho đối tượng ReaderRequest

        // Cập nhật thông tin độc giả trong cơ sở dữ liệu
        try {
            readerDAO.updateReader(readerRequest);
            mainController.loadPage("Reader.fxml", "Reader updated successfully!",  "#2ECC71");
        } catch (Exception e) {
            showNotification("Failed to update reader in the database! Please try again.", "#E74C3C"); // Màu đỏ
            e.printStackTrace();
        }

    }


    @FXML
    private void deleteReader(ActionEvent event) {
        // Lấy sách đang chọn từ bảng
        Reader selectedReader = readerTable.getSelectionModel().getSelectedItem();

        if (selectedReader != null) {
            // Hiển thị hộp thoại xác nhận
            Alert confirmationAlert = new Alert(Alert.AlertType.CONFIRMATION);
            confirmationAlert.setTitle("Confirm Deletion");
            confirmationAlert.setHeaderText("Are you sure you want to delete this reader?");
            confirmationAlert.setContentText(selectedReader.getFullName());

            confirmationAlert.showAndWait().ifPresent(response -> {
                if (response == ButtonType.OK) {
                    try {
                        readerDAO.deleteReader(selectedReader.getReaderId()); // Gọi phương thức xóa

                        // Xóa tài khoản khỏi TableView
                        readerTable.getItems().remove(selectedReader);
                        showNotification("Delete reader successfully!", "#2ECC71"); // Màu xanh lá
                        handleRefresh();
                    } catch (Exception e) {
                        Alert errorAlert = new Alert(Alert.AlertType.ERROR);
                        errorAlert.setTitle("Error Deleting reader");
                        errorAlert.setHeaderText(null);
                        errorAlert.setContentText("An error occurred while deleting the reader: " + e.getMessage());
                        errorAlert.showAndWait();
                    }
                }
            });
        } else {
            // Hiển thị thông báo nếu không có tài khoản nào được chọn
            showNotification("Please choose a reader to delete!", "#FF9224"); // Màu xanh lá

        }
    }

    private String generateNextReaderCode(String lastReaderCode) {
        if (lastReaderCode == null || lastReaderCode.isEmpty()) {
            return "REA0001";  // Nếu không có sách nào trong DB
        }

        // Tách phần số ra khỏi mã sách (Ví dụ: BOOK012 -> 12)
        int currentCode = Integer.parseInt(lastReaderCode.substring(3));
        int newCode = currentCode + 1;  // Tăng số lên

        // Sinh mã mới với định dạng BOOKxxx
        return "REA" + String.format("%04d", newCode);
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
        citizenshipCardField.clear();
        addressField.clear();
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
