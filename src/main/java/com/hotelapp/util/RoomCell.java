package com.hotelapp.util;

import com.hotelapp.controller.DashboardCustomerController;
import com.hotelapp.controller.RoomCellController;
import com.hotelapp.model.Room;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.ListCell;
import javafx.scene.layout.HBox;

import java.io.IOException;

public class RoomCell extends ListCell<Room> {
    private FXMLLoader loader;
    private HBox root;
    private RoomCellController controller;
    private DashboardCustomerController dashboardController;

    public RoomCell(DashboardCustomerController dashboardController) {
        this.dashboardController = dashboardController;
    }

    protected void updateItem(Room room, boolean empty) {
        super.updateItem(room, empty);
        try {
            if (empty || room == null) {
                setGraphic(null);
            } else {
                if (loader == null) {
                    loader = new FXMLLoader(getClass().getResource("/com/hotelapp/fxml/RoomCell.fxml"));
                    try {
                        root = loader.load();
                        controller = loader.getController();
                    } catch (IOException e) {
                        System.err.println("❌ Failed to load RoomCell.fxml: " + e.getMessage());
                        return;
                    }
                }

                if (controller != null) {
                    controller.setRoomData(room, dashboardController);
                    setGraphic(root);
                }
            }
        } catch (Exception e) {
            System.err.println("❌ Error in RoomCell.updateItem(): " + e.getMessage());
        }
    }

}
