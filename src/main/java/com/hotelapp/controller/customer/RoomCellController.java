package com.hotelapp.controller.customer;


import com.hotelapp.dao.RoomTypeDAO;
import com.hotelapp.model.RoomType;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.text.NumberFormat;
import java.util.Locale;

public class RoomCellController {

    @FXML private ImageView roomImage;
    @FXML private Label roomTypeLabel;
    @FXML private Label priceLabel;
    @FXML private Label availabilityLabel;
    @FXML private Button detailButton;
    @FXML private Button bookButton;

    private RoomType roomType;
    private DashboardCustomerController dashboardController;

    public void setRoomTypeData(RoomType roomType, DashboardCustomerController dashboardController) {
        this.roomType = roomType;
        this.dashboardController = dashboardController;

        roomTypeLabel.setText(roomType.getName());

        NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("id", "ID"));
        priceLabel.setText(currencyFormat.format(roomType.getPrice()));

        availabilityLabel.setText(roomType.getAvailableRoomCount() + " kamar tersedia");
        if (roomType.getAvailableRoomCount() <= 3) {
            availabilityLabel.setStyle("-fx-text-fill: #E67E22;");
        } else {
            availabilityLabel.setStyle("-fx-text-fill: #27AE60;");
        }

        try {
            String imageUrlPath = roomType.getImageUrl();
            if (imageUrlPath != null && !imageUrlPath.isBlank()) {
                String imagePath = imageUrlPath.startsWith("/") ? imageUrlPath : "/com/hotelapp/images/" + imageUrlPath;
                Image img = new Image(getClass().getResource(imagePath).toExternalForm());
                roomImage.setImage(img);
            } else {
                loadDefaultImage();
            }
        } catch (Exception e) {
            loadDefaultImage();
        }

        detailButton.setOnAction(event -> openRoomDetail(roomType));

        bookButton.setOnAction(event -> {
            if (this.dashboardController != null) {
                this.dashboardController.openBooking(this.roomType);
            }
        });
    }


    private void openRoomDetail(RoomType roomType) {
        try {
            RoomType detailedRoomType = RoomTypeDAO.getRoomTypeWithFacilitiesById(roomType.getId());
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/hotelapp/fxml/customer/RoomDetail.fxml"));
            Parent detailRoot = loader.load();
            RoomDetailController detailController = loader.getController();

            detailController.setRoomType(detailedRoomType);
            detailController.setDashboardController(this.dashboardController);
            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setTitle("Detail Tipe Kamar - " + detailedRoomType.getName());
            stage.setScene(new Scene(detailRoot));
            stage.showAndWait();

        } catch (Exception e) {
            System.err.println("Gagal memuat halaman detail kamar.");
            e.printStackTrace();
        }
    }

    private void loadDefaultImage() {
        try {
            String defaultPath = "/com/hotelapp/images/default_room.png";
            Image defaultImg = new Image(getClass().getResource(defaultPath).toExternalForm());
            roomImage.setImage(defaultImg);
        } catch (Exception e) {
            System.err.println("Gagal memuat gambar default.");
        }
    }
}