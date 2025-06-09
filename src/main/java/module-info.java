module com.hotelapp {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.web;

    requires org.controlsfx.controls;
    requires net.synedra.validatorfx;
    requires org.kordamp.ikonli.javafx;
    requires org.kordamp.bootstrapfx.core;
    requires eu.hansolo.tilesfx;
    requires java.sql;
    requires java.mail;
    requires com.google.zxing;
    requires itextpdf;
    requires activation;

    opens com.hotelapp.images to javafx.graphics;
    opens com.hotelapp to javafx.fxml;
    opens com.hotelapp.controller to javafx.fxml;
    opens com.hotelapp.model to javafx.fxml;
    opens com.hotelapp.dao to javafx.fxml;
    opens com.hotelapp.util to javafx.fxml;
    exports com.hotelapp;
    exports com.hotelapp.controller;
    exports com.hotelapp.model;
    exports com.hotelapp.dao;
    exports com.hotelapp.util;
}
