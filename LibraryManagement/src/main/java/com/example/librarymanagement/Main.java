package com.example.librarymanagement;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        Parent root = FXMLLoader.load(getClass().getResource("fxml/Login.fxml"));
        primaryStage.setTitle("Library Management System");
        primaryStage.setResizable(false);
        primaryStage.setScene(new Scene(root, 700, 500)); // Kích thước cửa sổ
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
