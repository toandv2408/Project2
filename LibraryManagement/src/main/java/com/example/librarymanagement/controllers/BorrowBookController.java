package com.example.librarymanagement.controllers;

import com.example.librarymanagement.dao.BorrowBookDAO;
import com.example.librarymanagement.dao.BorrowCardDAO;
import com.example.librarymanagement.models.BorrowBook;
import com.example.librarymanagement.models.BorrowCard;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.util.List;

public class BorrowBookController {

    @FXML
    private TableView<BorrowBook> borrowTable;
    @FXML
    private TableColumn<BorrowBook, Integer> idColumn;
    @FXML
    private TableColumn<BorrowBook, String> cardCodeColumn;
    @FXML
    private TableColumn<BorrowBook, String> fullNameColumn;
    @FXML
    private TableColumn<BorrowBook, String> bookCodeColumn;
    @FXML
    private TableColumn<BorrowBook, String> borrowDateColumn;
    @FXML
    private TableColumn<BorrowBook, String> dueDateColumn;
    @FXML
    private TableColumn<BorrowBook, String> returnDateColumn;
    @FXML
    private TableColumn<BorrowBook, Integer> stockColumn;
    @FXML
    private TableColumn<BorrowBook, String> statusColumn;
    @FXML
    private TextField searchField;
    @FXML
    private DatePicker borrowDatePicker;
    @FXML
    private TextField quantityField;

    BorrowBookDAO borrowBookDAO = new BorrowBookDAO();

    private ObservableList<BorrowBook> borrowBookList = FXCollections.observableArrayList();

    public void initialize() {
        // Set up the columns
        setupTableColumns();
        loadBorrowedBooks();
    }

    private void loadBorrowedBooks() {
        List<BorrowBook> borrowBooks = borrowBookDAO.getAllBorrowingRecords();
        borrowBooks.sort((i1, i2) -> i2.getBorrowedDate().compareTo(i1.getBorrowedDate()));
        borrowBookList.setAll(borrowBooks);
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        cardCodeColumn.setCellValueFactory(new PropertyValueFactory<>("cardCode"));
        fullNameColumn.setCellValueFactory(new PropertyValueFactory<>("fullName"));
        bookCodeColumn.setCellValueFactory(new PropertyValueFactory<>("bookCode"));
        borrowDateColumn.setCellValueFactory(new PropertyValueFactory<>("borrowedDate"));
        dueDateColumn.setCellValueFactory(new PropertyValueFactory<>("dueDate"));
        returnDateColumn.setCellValueFactory(new PropertyValueFactory<>("returnDate"));
        stockColumn.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));
        borrowTable.setItems(borrowBookList);
        setupPagination();
    }

    private void setupPagination() {
    }

    private void setupTableColumns() {
    }

    @FXML
    private void handleExportBorrowBookRecords() {
        // Logic for exporting borrowed book records
        System.out.println("Exporting borrowed book records...");
        // Add your export logic here
    }

    @FXML
    private void handleBorrowBook() {
        // Logic for borrowing a book
        String bookCode = ""; // Get book code from the UI (TextField or similar)
        String cardCode = ""; // Get card code from the UI (TextField or similar)
        int quantity = Integer.parseInt(quantityField.getText());
        String borrowDate = borrowDatePicker.getValue().toString();

        // Validate inputs
        if (bookCode.isEmpty() || cardCode.isEmpty() || quantity <= 0) {
            showAlert("Error", "Please fill all fields correctly.");
            return;
        }

        // Process borrowing logic
        System.out.println("Borrowing book: " + bookCode + " for reader: " + cardCode);
        // Implement the borrowing logic (e.g., database update)
    }

    private void loadReaderData() {
        // Logic to load data into the table
        // This method should fetch data from your data source (e.g., database)
        // Example: readerTable.setItems(readerData);
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
