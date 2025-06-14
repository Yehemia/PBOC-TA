package com.hotelapp;        // pastikan ini sesuai package folder Anda

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws IOException {
        Parent root = FXMLLoader.load(getClass().getResource("/com/hotelapp/fxml/login.fxml"));
        Scene scene = new Scene(root);
        primaryStage.setWidth(920);
        primaryStage.setHeight(710);
        primaryStage.setMinWidth(850);
        primaryStage.setMinHeight(680);
        primaryStage.setTitle("Sistem Reservasi Hotel");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}