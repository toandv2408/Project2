package com.example.librarymanagement.controllers;

import com.example.librarymanagement.dao.InvoiceDAO;
import com.example.librarymanagement.models.Book;
import com.example.librarymanagement.models.Invoice;
import com.example.librarymanagement.models.Reader;
import javafx.animation.FadeTransition;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.util.Callback;
import javafx.util.Duration;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Timestamp;
import java.text.NumberFormat;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

public class InvoiceController {

    @FXML
    private ImageView QRCode;
    @FXML
    private TextField idField;

    @FXML
    private TableColumn<Invoice, Integer> amountColumn;

    @FXML
    private Label amountLabel;

    @FXML
    private TableColumn<Invoice, Integer> idColumn;

    @FXML
    private TableColumn<Invoice, String> invoiceCodeColumn;

    @FXML
    private Label invoiceCodeLabel;

    @FXML
    private TableView<Invoice> invoiceTable;

    @FXML
    private TableColumn<Invoice, Timestamp> paymentDateColumn;

    @FXML
    private TableColumn<Invoice, String> paymentTypeColumn;

    @FXML
    private TableColumn<Invoice, String> realatedCodeColumn;

    @FXML
    private TableColumn<Invoice, String> relatedNameColumn;

    @FXML
    private TextField searchField;
    @FXML
    private VBox notificationBox;
    @FXML
    private Label notificationLabel;

    @FXML
    private TableColumn<Invoice, String> statusColumn;

    InvoiceDAO invoiceDAO = new InvoiceDAO();
    private ObservableList<Invoice> invoiceList = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        loadInvoiceData();
        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            filterInvoices(newValue); // Gọi phương thức lọc khi có sự thay đổi
        });
        invoiceTable.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                updateInvoiceDetails(newValue);
            }
        });
        amountColumn.setCellFactory(new Callback<TableColumn<Invoice, Integer>, TableCell<Invoice, Integer>>() {
            @Override
            public TableCell<Invoice, Integer> call(TableColumn<Invoice, Integer> column) {
                return new TableCell<Invoice, Integer>() {
                    @Override
                    protected void updateItem(Integer amount, boolean empty) {
                        super.updateItem(amount, empty);
                        if (empty || amount == null) {
                            setText(null);
                        } else {
                            // Định dạng giá với dấu chấm và thêm 'vnđ'
                            NumberFormat formatter = NumberFormat.getNumberInstance(new Locale("vi", "VN"));
                            setText(formatter.format(amount) + "đ");
                        }
                    }
                };
            }
        });
    }

    private void filterInvoices(String keyword) {
        ObservableList<Invoice> filteredList = FXCollections.observableArrayList();

        for (Invoice invoice : invoiceList) {
            if (invoice.getInvoiceCode().toLowerCase().contains(keyword.toLowerCase()) ||
                    invoice.getRelatedCodeOwnerName().toLowerCase().contains(keyword.toLowerCase()) ||
                    invoice.getPaymentType().toLowerCase().contains(keyword.toLowerCase()) ||
                    invoice.getStatus().toLowerCase().contains(keyword.toLowerCase()) ||
                    invoice.getRelatedCode().toLowerCase().contains(keyword.toLowerCase())) {
                filteredList.add(invoice);
            }
        }

        invoiceTable.setItems(filteredList);
    }

    private void loadInvoiceData() {
        List<Invoice> invoices = invoiceDAO.getPayments();
        invoices.sort((i1, i2) -> i2.getPaymentDate().compareTo(i1.getPaymentDate()));
        invoiceList.setAll(invoices);
        idColumn.setCellValueFactory(new PropertyValueFactory<>("paymentId"));
        invoiceCodeColumn.setCellValueFactory(new PropertyValueFactory<>("invoiceCode"));
        paymentTypeColumn.setCellValueFactory(new PropertyValueFactory<>("paymentType"));
        amountColumn.setCellValueFactory(new PropertyValueFactory<>("amount"));
        paymentDateColumn.setCellValueFactory(new PropertyValueFactory<>("paymentDate"));
        statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));
        realatedCodeColumn.setCellValueFactory(new PropertyValueFactory<>("relatedCode")); // Thiết lập relatedCode
        relatedNameColumn.setCellValueFactory(new PropertyValueFactory<>("relatedCodeOwnerName")); // Thiết lập relatedCodeOwnerName
        invoiceTable.setItems(invoiceList);
    }

    private void updateInvoiceDetails(Invoice selectedInvoice) {
        idField.setText(String.valueOf(selectedInvoice.getPaymentId()));
        amountLabel.setText(formatAmount(selectedInvoice.getAmount()));
        invoiceCodeLabel.setText(selectedInvoice.getInvoiceCode());
    }
    private String formatAmount(Integer amount) {
        // Định dạng giá trị cho amountLabel
        NumberFormat formatter = NumberFormat.getNumberInstance(new Locale("vi", "VN"));
        return formatter.format(amount) + "đ";
    }
    @FXML
    private void handleExportInvoices(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV Files", "*.csv"));
        File file = fileChooser.showSaveDialog(null);

        if (file != null) {
            exportReadersToCSV(invoiceList, file.getAbsolutePath());
        }
    }

    public void exportReadersToCSV(List<Invoice> invoiceList, String filePath) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");
        try (FileWriter writer = new FileWriter(filePath)) {
            // Viết tiêu đề cho file CSV
            writer.append("Invoice Code,Related Code,Related Name,Payment Type,Amount,Payment Date,Status\n");

            // Viết thông tin sách
            for (Invoice invoice : invoiceList) {
                writer.append(invoice.getInvoiceCode())
                        .append(",")
                        .append(invoice.getRelatedCode())
                        .append(",")
                        .append(invoice.getRelatedCodeOwnerName())
                        .append(",")
                        .append(invoice.getPaymentType())
                        .append(",")
                        .append(String.valueOf(invoice.getAmount()))
                        .append(",")
                        .append(invoice.getPaymentDate().toLocalDateTime().format(formatter))
                        .append(",")
                        .append(invoice.getStatus())
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

    @FXML
    void handlePayAction(ActionEvent event) {
        // Lấy hóa đơn đã chọn từ bảng
        Invoice selectedInvoice = invoiceTable.getSelectionModel().getSelectedItem();
        if (selectedInvoice != null) {
            String newStatus = "paid"; // Trạng thái mới
            Timestamp paymentDate = new Timestamp(System.currentTimeMillis()); // Ngày thanh toán hiện tại

            boolean updated = invoiceDAO.updateInvoiceStatus(selectedInvoice.getPaymentId(), newStatus, paymentDate);
            if (updated) {
                loadInvoiceData();
                updateInvoiceDetails(selectedInvoice);
                // Thông báo thành công
                showNotification("Pay successfully!", "#2ECC71"); // Màu xanh lá
            } else {
                // Thông báo lỗi
                showNotification("Pay fail!", "#FF9224"); // Màu xanh lá
            }
        } else {
            showNotification("Please choose a invoice to pay!", "#FF9224"); // Màu xanh lá
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
}
