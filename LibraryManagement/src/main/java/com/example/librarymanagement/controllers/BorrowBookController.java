package com.example.librarymanagement.controllers;

import com.example.librarymanagement.dao.BorrowBookDAO;
import com.example.librarymanagement.dao.BorrowCardDAO;
import com.example.librarymanagement.models.Book;
import com.example.librarymanagement.models.BorrowBook;
import com.example.librarymanagement.models.BorrowCard;
import com.example.librarymanagement.models.Reader;
import javafx.animation.FadeTransition;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.util.Duration;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.util.*;

public class BorrowBookController {
    private static final int ROWS_PER_PAGE = 18;
    @FXML
    private Pagination pagination;
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
    private ComboBox<String> bookCodeComboBox;
    @FXML
    private Label titleLabel;  // Nhãn để hiện tiêu đề sách
    @FXML
    private Label authorLabel; // Nhãn để hiện tác giả sách
    @FXML
    private Label genreLabel;  // Nhãn để hiện thể loại sách
    @FXML
    private Label stockLabel;
    @FXML
    private Label priceLabel;
    @FXML
    private Label yearLabel;
    @FXML
    private ComboBox<String> cardCodeComboBox;
    @FXML
    private Label fullNameLabel;

    @FXML
    private Label borrowDateLabel;
    @FXML
    private DatePicker dueDatePicker;
    @FXML
    private TextField quantityField;
    @FXML
    private VBox notificationBox;
    @FXML
    private Label notificationLabel;

    BorrowBookDAO borrowBookDAO = new BorrowBookDAO();

    private ObservableList<BorrowBook> borrowBookList = FXCollections.observableArrayList();
    private ObservableList<String> bookCodes = FXCollections.observableArrayList();

    private Map<String, Book> booksData = new HashMap<>();

    public void initialize() {
        // Set up the columns
        loadBorrowedBooks();

        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            filterBorrowBookRecords(newValue); // Gọi phương thức lọc khi có sự thay đổi
        });

        List<String> bookCodes = borrowBookDAO.getAllBookCodes();
        bookCodeComboBox.setItems(FXCollections.observableArrayList(bookCodes));

        List<String> cardCodes = borrowBookDAO.getAllCardCodes();
        cardCodeComboBox.setItems(FXCollections.observableArrayList(cardCodes));

        // Lắng nghe sự thay đổi khi người dùng chọn mã sách
        bookCodeComboBox.setOnAction(event -> {
            String selectedBookCode = bookCodeComboBox.getValue();
            if (selectedBookCode != null) {
                displayBookDetails(selectedBookCode);
            }
        });
        cardCodeComboBox.setOnAction(event -> {
            String selectedCardCode = cardCodeComboBox.getValue();
            if (selectedCardCode != null) {
                displayFullName(selectedCardCode);
            }
        });
    }

    private void filterBorrowBookRecords(String keyword) {
        ObservableList<BorrowBook> filteredList = FXCollections.observableArrayList();

        // Lọc danh sách tài khoản
        for (BorrowBook borrowBook : borrowBookList) {
            if (borrowBook.getBookCode().toLowerCase().contains(keyword.toLowerCase()) ||
                    borrowBook.getFullName().toLowerCase().contains(keyword.toLowerCase()) ||
                    borrowBook.getCardCode().toLowerCase().contains(keyword.toLowerCase()) ||
                    borrowBook.getStatus().toLowerCase().contains(keyword.toLowerCase())) {
                filteredList.add(borrowBook);
            }
        }

        // Cập nhật TableView với danh sách đã lọc
        borrowTable.setItems(filteredList);
    }

    private void displayFullName(String cardCode) {
        BorrowCard card = borrowBookDAO.getFullNameByCode(cardCode); // Bạn cần tạo phương thức này trong BorrowBookDAO
        if (card != null) {
            fullNameLabel.setText(card.getFullName());
            borrowDateLabel.setText(String.valueOf(LocalDate.now()));
        } else {
            fullNameLabel.setText("Tên không tìm thấy");
        }
    }

    private void displayBookDetails(String bookCode) {
        Book book = borrowBookDAO.getBookByCode(bookCode);
        if (book != null) {
            titleLabel.setText(book.getTitle());
            authorLabel.setText(book.getAuthor());
            genreLabel.setText(book.getGenreName());
            yearLabel.setText(String.valueOf(book.getPublishedYear()));
            stockLabel.setText(String.valueOf(book.getStock()));
            priceLabel.setText(formatPrice(book.getPrice()));
        }
    }

    private String formatPrice(int price) {
        // Tạo NumberFormat cho định dạng tiền tệ của Việt Nam
        NumberFormat numberFormat = NumberFormat.getInstance(new Locale("vi", "VN"));
        return numberFormat.format(price) + "đ";
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
        int totalPageCount = (int) Math.ceil((double) borrowBookList.size() / ROWS_PER_PAGE);
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
        int toIndex = Math.min(fromIndex + ROWS_PER_PAGE, borrowBookList.size());

        borrowTable.setItems(FXCollections.observableArrayList(borrowBookList.subList(fromIndex, toIndex)));
    }


    @FXML
    private void handleExportBorrowBookRecords() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV Files", "*.csv"));
        File file = fileChooser.showSaveDialog(null);

        if (file != null) {
            exportReadersToCSV(borrowBookList, file.getAbsolutePath());
        }
    }

    public void exportReadersToCSV(List<BorrowBook> borrowBookList, String filePath) {
        try (FileWriter writer = new FileWriter(filePath)) {
            writer.append("Card Code,Full Name,Book Code,Borrowed Date,Due Date,Return Date,Quantity,Status\n");

            // Viết thông tin sách
            for (BorrowBook borrowBook : borrowBookList) {
                writer.append(borrowBook.getCardCode())
                        .append(",")
                        .append(borrowBook.getFullName())
                        .append(",")
                        .append(borrowBook.getBookCode())
                        .append(",")
                        .append(borrowBook.getBorrowedDate().toString())
                        .append(",")
                        .append(borrowBook.getDueDate().toString())
                        .append(",")
                        .append(borrowBook.getReturnDate() != null ? borrowBook.getReturnDate().toString() : "Not Returned")
                        .append(",")
                        .append(String.valueOf(borrowBook.getQuantity()))
                        .append(",")
                        .append(borrowBook.getStatus())
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

    public void handleBorrowBook(ActionEvent event) {
        // Lấy mã sách và mã thẻ từ ComboBox
        String selectedBookCode = bookCodeComboBox.getValue();
        String selectedCardCode = cardCodeComboBox.getValue();

        // Kiểm tra mã sách và mã thẻ đã được chọn chưa
        if (selectedBookCode == null || selectedCardCode == null) {
            showNotification("Please select book code and card code.", "#FF9224");
            return;
        }

        // Kiểm tra số lượng mượn
        int quantity;
        try {
            quantity = Integer.parseInt(quantityField.getText());
        } catch (NumberFormatException e) {
            showNotification("Invalid loan amount.", "#FF9224");
            return;
        }

        if (quantity <= 0) {
            showNotification("The borrowed amount must be greater than 0.", "#FF9224");
            return;
        }

        // Lấy thông tin sách từ DAO
        Book book = borrowBookDAO.getBookByCode(selectedBookCode);
        if (book == null) {
            showNotification("No books found with this code.", "#FF9224");
            return;
        }

        // Kiểm tra tồn kho
        if (book.getStock() < quantity) {
            showNotification("There are not enough books in stock to borrow.", "#FF9224");
            return;
        }

        // Lấy thông tin người mượn từ DAO
        BorrowCard card = borrowBookDAO.getFullNameByCode(selectedCardCode);
        if (card == null) {
            showNotification("No cards found with this code.", "#FF9224");
            return;
        }

        // Lấy ngày mượn và ngày trả
        LocalDate borrowDate = LocalDate.now();
        LocalDate dueDate = dueDatePicker.getValue();
        if (dueDate == null) {
            showNotification("Please select a return date.", "#FF9224");
            return;
        }

        Book selectedBook = borrowBookDAO.getBookByCode(selectedBookCode);
        BorrowCard selectedCard = borrowBookDAO.getFullNameByCode(selectedCardCode);
        // Tạo bản ghi mượn sách
        if (selectedBook != null) { // Kiểm tra xem sách có tồn tại hay không
            BorrowBook borrowBook = new BorrowBook();
            borrowBook.setCardCode(selectedCardCode);
            borrowBook.setBorrowCardId(selectedCard.getBorrowCardId());
            borrowBook.setBook(selectedBook); // Thiết lập đối tượng Book
            borrowBook.setBorrowedDate(borrowDate);
            borrowBook.setDueDate(dueDate);
            borrowBook.setQuantity(quantity);
            borrowBook.setStatus("active");

            // Gọi phương thức lưu
            boolean success = borrowBookDAO.saveBorrowBook(borrowBook);
            if (success) {
                book = borrowBook.getBook();
                int newStock = book.getStock() - borrowBook.getQuantity(); // Tính số lượng sách mới

                // Cập nhật số lượng sách
                book.setStock(newStock);
                borrowBookDAO.updateBookStock(book);
                System.out.println("Borrow record saved successfully.");
                showNotification("Borrow record saved successfully!" , "#2ECC71"); // Màu xanh lá
                loadBorrowedBooks();
            } else {
                System.out.println("Failed to save borrowing record.");
                showNotification("Failed to save borrowing record." , "red"); // Màu xanh lá
            }
        } else {
            System.out.println("The selected book does not exist.");
            showNotification("The selected book does not exist.", "#FF9224");
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

    public void handleCancel(ActionEvent event) {
        bookCodeComboBox.setValue(null);
        cardCodeComboBox.setValue(null);
        dueDatePicker.setValue(null);
        quantityField.clear();
        titleLabel.setText("");
        authorLabel.setText("");
        genreLabel.setText("");
        yearLabel.setText("");
        stockLabel.setText("");
        priceLabel.setText("");
        fullNameLabel.setText("");
        borrowDateLabel.setText("");
    }
}
