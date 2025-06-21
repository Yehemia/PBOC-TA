package com.hotelapp.util;

import com.hotelapp.controller.customer.DashboardCustomerController;
import com.hotelapp.controller.customer.RoomCellController;
import com.hotelapp.model.RoomType; // <-- DIUBAH dari Room ke RoomType
import javafx.fxml.FXMLLoader;
import javafx.scene.control.ListCell;
import javafx.scene.layout.VBox; // <-- Diubah dari HBox ke VBox sesuai FXML kartu baru kita

import java.io.IOException;

// 1. Kelas sekarang bekerja dengan objek RoomType
public class RoomCell extends ListCell<RoomType> {
    private FXMLLoader loader;
    private VBox root; // Menggunakan VBox sebagai root dari RoomCell.fxml
    private RoomCellController controller;
    private DashboardCustomerController dashboardController;

    public RoomCell(DashboardCustomerController dashboardController) {
        this.dashboardController = dashboardController;
    }

    // 2. Metode updateItem sekarang menerima objek RoomType
    @Override
    protected void updateItem(RoomType roomType, boolean empty) {
        super.updateItem(roomType, empty);

        if (empty || roomType == null) {
            setText(null);
            setGraphic(null);
        } else {
            if (loader == null) {
                loader = new FXMLLoader(getClass().getResource("/com/hotelapp/fxml/customer/RoomCell.fxml"));
                try {
                    root = loader.load();
                    controller = loader.getController();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            if (controller != null) {
                // 3. Memanggil metode baru yang benar di RoomCellController
                controller.setRoomTypeData(roomType, dashboardController);
                setGraphic(root);
            }
        }
    }
}