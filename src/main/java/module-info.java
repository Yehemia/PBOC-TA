module com.hotelapp {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.web;

    requires org.controlsfx.controls;
    requires net.synedra.validatorfx;
    requires org.kordamp.ikonli.javafx;
    requires eu.hansolo.tilesfx;
    requires java.sql;
    requires java.mail;
    requires com.google.zxing;
    requires itextpdf;
    requires activation;
    requires org.kordamp.ikonli.fontawesome;
    requires org.kordamp.ikonli.fontawesome5;
    requires org.kordamp.ikonli.core;
    requires jbcrypt;
    requires java.desktop;
    uses org.kordamp.ikonli.IkonProvider;

    opens com.hotelapp.images to javafx.graphics;
    opens com.hotelapp to javafx.fxml;
    opens com.hotelapp.model to javafx.fxml;
    opens com.hotelapp.dao to javafx.fxml;
    opens com.hotelapp.util to javafx.fxml;
    opens com.hotelapp.controller.customer to javafx.fxml;
    opens com.hotelapp.controller.login to javafx.fxml;
    opens com.hotelapp.controller.resepsionis to javafx.fxml;
    opens com.hotelapp.controller.admin to javafx.fxml;
    exports com.hotelapp;
    exports com.hotelapp.model;
    exports com.hotelapp.dao;
    exports com.hotelapp.util;
    exports com.hotelapp.controller.customer;
    exports com.hotelapp.controller.login;
    exports com.hotelapp.controller.resepsionis;
    exports com.hotelapp.controller.admin;
}
