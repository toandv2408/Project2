module com.example.librarymanagement {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;
    requires jbcrypt;
    requires java.desktop;
    requires fontawesomefx; // Nếu bạn đang sử dụng JDBC cho cơ sở dữ liệu

    opens com.example.librarymanagement.controllers to javafx.fxml;
    opens com.example.librarymanagement.models; // Thêm dòng này nếu bạn có các class trong models
    exports com.example.librarymanagement;
}
