package com.example.librarymanagement.controllers;

import com.example.librarymanagement.dao.AccountDAO;
import com.example.librarymanagement.dao.BookDAO;
import com.example.librarymanagement.models.Book;
import com.example.librarymanagement.request.BookRequest;
import com.example.librarymanagement.respone.AccountResponse;
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

import java.io.*;
import java.sql.SQLException;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class BookController {

    @FXML
    private TableView<Book> booksTable;

    @FXML
    private TableColumn<Book, Long> idColumn;

    @FXML
    private TableColumn<Book, String> bookCodeColumn;

    @FXML
    private TableColumn<Book, String> titleColumn;

    @FXML
    private TableColumn<Book, String> authorColumn;

    @FXML
    private TableColumn<Book, Integer> publishedYearColumn;

    @FXML
    private TableColumn<Book, String> genreNameColumn;

    @FXML
    private TableColumn<Book, Integer> stockColumn;

    @FXML
    private TableColumn<Book, Integer> priceColumn;
    @FXML
    private TextField idField;

    @FXML
    private TextField titleField;

    @FXML
    private TextField authorField;

    @FXML
    private TextField publishedYearField;

    @FXML
    private TextField genreNameField;

    @FXML
    private TextField stockField;

    @FXML
    private TextField priceField;

    @FXML
    private TextField searchField;
    @FXML
    private VBox notificationBox;
    @FXML
    private Label notificationLabel;

    private ObservableList<Book> bookList = FXCollections.observableArrayList();

    // Phương thức khởi tạo và hiển thị dữ liệu
    @FXML
    public void initialize() {
        loadBookData();
        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            filterAccounts(newValue); // Gọi phương thức lọc khi có sự thay đổi
        });
        priceColumn.setCellFactory(new Callback<TableColumn<Book, Integer>, TableCell<Book, Integer>>() {
            @Override
            public TableCell<Book, Integer> call(TableColumn<Book, Integer> column) {
                return new TableCell<Book, Integer>() {
                    @Override
                    protected void updateItem(Integer price, boolean empty) {
                        super.updateItem(price, empty);
                        if (empty || price == null) {
                            setText(null);
                        } else {
                            // Định dạng giá với dấu chấm và thêm 'vnđ'
                            NumberFormat formatter = NumberFormat.getNumberInstance(new Locale("vi", "VN"));
                            setText(formatter.format(price) + "đ");
                        }
                    }
                };
            }
        });
        booksTable.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                showBookDetails(newValue);  // Hiển thị thông tin lên form
            }
        });
    }

    // Phương thức để lấy dữ liệu sách từ DAO và hiển thị lên TableView
    private void loadBookData() {
        BookDAO bookDAO = new BookDAO();
        List<Book> books = bookDAO.getAllBooks();

        // Chuyển danh sách sách thành ObservableList để hiển thị trên TableView
        bookList.setAll(books);

        // Đặt giá trị cho các cột
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        bookCodeColumn.setCellValueFactory(new PropertyValueFactory<>("bookCode"));
        titleColumn.setCellValueFactory(new PropertyValueFactory<>("title"));
        authorColumn.setCellValueFactory(new PropertyValueFactory<>("author"));
        publishedYearColumn.setCellValueFactory(new PropertyValueFactory<>("publishedYear"));
        genreNameColumn.setCellValueFactory(new PropertyValueFactory<>("genreName"));
        stockColumn.setCellValueFactory(new PropertyValueFactory<>("stock"));
        priceColumn.setCellValueFactory(new PropertyValueFactory<>("price"));

        // Đặt dữ liệu vào TableView
        booksTable.setItems(bookList);
    }

    private void showBookDetails(Book book) {
        idField.setText(String.valueOf(book.getId()));
        titleField.setText(book.getTitle());
        authorField.setText(book.getAuthor());
        publishedYearField.setText(String.valueOf(book.getPublishedYear()));
        genreNameField.setText(book.getGenreName());
        stockField.setText(String.valueOf(book.getStock()));
        priceField.setText(String.valueOf(book.getPrice()));
    }

    private BookDAO bookDAO;

    public BookController() {
        bookDAO = new BookDAO();
    }

    // Phương thức để thêm sách mới
    @FXML
    private void handleExportBooks(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV Files", "*.csv"));
        File file = fileChooser.showSaveDialog(null);

        if (file != null) {
            exportBooksToCSV(bookList, file.getAbsolutePath());
        }
    }

    @FXML
    private void handleImportBooks(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV Files", "*.csv"));
        File file = fileChooser.showOpenDialog(null);

        if (file != null) {
            importBooksFromCSV(file.getAbsolutePath());
            loadBookData(); // Tải lại dữ liệu sách sau khi import
        }
    }


    @FXML
    private void addBook(ActionEvent event) {
        // Assuming bookService.getLastBookCode() retrieves the last book code from the database
        String lastBookCode = bookDAO.getLastBookCode();

        // Generate the new book code
        String newBookCode = generateNextBookCode(lastBookCode);

        // Retrieve the input from the fields
        String title = titleField.getText();
        String author = authorField.getText();
        String genreName = genreNameField.getText();
        String publishedYearStr = publishedYearField.getText();
        String stockStr = stockField.getText();
        String priceStr = priceField.getText();

        // Check if any fields are empty
        if (title.isEmpty() || author.isEmpty() || genreName.isEmpty() ||
                publishedYearStr.isEmpty() || stockStr.isEmpty() || priceStr.isEmpty()) {
            showNotification("Please fill in all fields!", "#E74C3C"); // Màu đỏ
            return;
        }

        // Check if inputs are valid numbers
        if (!isNumeric(publishedYearStr) || !isNumeric(stockStr) || !isNumeric(priceStr)) {
            showNotification("Invalid input! Please enter valid numbers for year, stock, and price.", "#E74C3C"); // Màu đỏ
            return;
        }

        // Parse the input values
        int publishedYear = Integer.parseInt(publishedYearStr);
        int stock = Integer.parseInt(stockStr);
        int price = Integer.parseInt(priceStr);

        // Create a new book object
        BookRequest book = new BookRequest(title, author, publishedYear, genreName, stock, price, newBookCode);

        // Attempt to add the book to the database
        if (bookDAO.addBook(book)) {
            loadBookData();
            showNotification("Add book successfully!", "#2ECC71"); // Màu xanh lá
            handleRefresh(); // Refresh the table view after adding
        } else {
            showNotification("Failed to add book to the database! Please try again.", "#E74C3C"); // Màu đỏ
        }
    }

    // Helper method to check if a string is a valid number
    private boolean isNumeric(String str) {
        try {
            Integer.parseInt(str);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }


    // Phương thức sinh mã sách mới với số tăng dần
    private String generateNextBookCode(String lastBookCode) {
        if (lastBookCode == null || lastBookCode.isEmpty()) {
            return "BOOK0001";  // Nếu không có sách nào trong DB
        }

        // Tách phần số ra khỏi mã sách (Ví dụ: BOOK012 -> 12)
        int currentCode = Integer.parseInt(lastBookCode.substring(4));
        int newCode = currentCode + 1;  // Tăng số lên

        // Sinh mã mới với định dạng BOOKxxx
        return "BOOK" + String.format("%04d", newCode);
    }

    // Ví dụ thêm phương thức lấy tất cả sách
    public List<Book> getAllBooks() {
        return bookDAO.getAllBooks();
    }

    @FXML
    private void updateBook(ActionEvent event) {
        // Lấy sách đang chọn từ bảng
        Book selectedBook = booksTable.getSelectionModel().getSelectedItem();

        if (selectedBook != null) {
            // Lấy thông tin từ form
            Long id = Long.parseLong(idField.getText());
            String title = titleField.getText();
            String author = authorField.getText();
            int publishedYear = Integer.parseInt(publishedYearField.getText());
            String genreName = genreNameField.getText();
            int stock = Integer.parseInt(stockField.getText());
            int price = Integer.parseInt(priceField.getText());

            String bookCode = selectedBook.getBookCode();

            Book updatedBook = new Book(id, title, author, publishedYear, genreName, stock, price, bookCode);

            bookDAO.updateBook(updatedBook);

            bookList.setAll(bookDAO.getAllBooks());
            showNotification("Update book successfully!", "#2ECC71"); // Màu xanh lá

            clearFields();
        } else {
            showNotification("Please choose a book to update!", "#FF9224"); // Màu xanh lá
        }
    }


    @FXML
    private void deleteBook(ActionEvent event) {
        // Lấy sách đang chọn từ bảng
        Book selectedBook = booksTable.getSelectionModel().getSelectedItem();

        if (selectedBook != null) {
            // Hiển thị hộp thoại xác nhận
            Alert confirmationAlert = new Alert(Alert.AlertType.CONFIRMATION);
            confirmationAlert.setTitle("Confirm Deletion");
            confirmationAlert.setHeaderText("Are you sure you want to delete this book?");
            confirmationAlert.setContentText(selectedBook.getTitle());

            confirmationAlert.showAndWait().ifPresent(response -> {
                if (response == ButtonType.OK) {
                    try {
                        bookDAO.deleteBook(selectedBook.getId()); // Gọi phương thức xóa

                        // Xóa tài khoản khỏi TableView
                        booksTable.getItems().remove(selectedBook);
                        showNotification("Delete book successfully!", "#2ECC71"); // Màu xanh lá
                        handleRefresh();
                    } catch (Exception e) {
                        Alert errorAlert = new Alert(Alert.AlertType.ERROR);
                        errorAlert.setTitle("Error Deleting Book");
                        errorAlert.setHeaderText(null);
                        errorAlert.setContentText("An error occurred while deleting the book: " + e.getMessage());
                        errorAlert.showAndWait();
                    }
                }
            });
        } else {
            // Hiển thị thông báo nếu không có tài khoản nào được chọn
            showNotification("Please choose a book to delete!", "#FF9224"); // Màu xanh lá

        }
    }

    public void exportBooksToCSV(List<Book> bookList, String filePath) {
        try (FileWriter writer = new FileWriter(filePath)) {
            // Viết tiêu đề cho file CSV
            writer.append("Book Code,Title,Author,Published Year,Genre Name,Stock,Price\n");

            // Viết thông tin sách
            for (Book book : bookList) {
                writer.append(book.getBookCode())
                        .append(",")
                        .append(book.getTitle())
                        .append(",")
                        .append(book.getAuthor())
                        .append(",")
                        .append(String.valueOf(book.getPublishedYear()))
                        .append(",")
                        .append(book.getGenreName())
                        .append(",")
                        .append(String.valueOf(book.getStock()))
                        .append(",")
                        .append(String.valueOf(book.getPrice()))
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

    public void importBooksFromCSV(String filePath) {
        String line;
        String csvSplitBy = ",";

        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            // Bỏ qua tiêu đề
            br.readLine();

            // Lấy mã sách cuối cùng để tạo mã mới
            String lastBookCode = bookDAO.getLastBookCode();

            while ((line = br.readLine()) != null) {
                String[] bookData = line.split(csvSplitBy);
                if (bookData.length >= 5) { // Đảm bảo có đủ dữ liệu
                    // Tạo mã sách tự động
                    String newBookCode = generateNextBookCode(lastBookCode);
                    lastBookCode = newBookCode; // Cập nhật mã sách cuối cùng cho lần sau

                    // Tạo đối tượng sách từ dữ liệu
                    BookRequest book = new BookRequest(
                            bookData[0], // title
                            bookData[1], // author
                            Integer.parseInt(bookData[2]), // publishedYear
                            bookData[3], // genreName
                            Integer.parseInt(bookData[4]), // stock
                            Integer.parseInt(bookData[5]), // price
                            newBookCode // Tạo mã sách tự động
                    );

                    // Thêm sách vào cơ sở dữ liệu
                    bookDAO.addBook(book);
                }
            }
            System.out.println("Import successful from: " + filePath);
            showNotification("Import successful from: " + filePath, "#2ECC71"); // Màu xanh lá

        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Import failed: " + e.getMessage());
            showNotification("Import failed: " + filePath, "red"); // Màu xanh lá
        } catch (NumberFormatException e) {
            System.out.println("Invalid number format in CSV: " + e.getMessage());
            showNotification("Invalid number format in CSV: " + filePath, "#FF9224"); // Màu xanh lá

        }
    }



    private void filterAccounts(String keyword) {
        ObservableList<Book> filteredList = FXCollections.observableArrayList();

        // Lọc danh sách tài khoản
        for (Book book : bookList) {
            if (book.getBookCode().toLowerCase().contains(keyword.toLowerCase()) ||
                    book.getTitle().toLowerCase().contains(keyword.toLowerCase()) ||
                    book.getAuthor().toLowerCase().contains(keyword.toLowerCase()) ||
                    book.getGenreName().toLowerCase().contains(keyword.toLowerCase()) ||
                    book.getPublishedYear().toString().contains(keyword.toLowerCase())) {
                filteredList.add(book);
            }
        }

        // Cập nhật TableView với danh sách đã lọc
        booksTable.setItems(filteredList);
    }

    @FXML
    private void handleRefresh() {
        clearFields();
    }

    private void clearFields() {
        idField.clear();
        titleField.clear();
        authorField.clear();
        publishedYearField.clear();
        genreNameField.clear();
        stockField.clear();
        priceField.clear();
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
