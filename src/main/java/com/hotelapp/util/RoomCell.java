package com.hotelapp.util;

import com.hotelapp.controller.customer.DashboardCustomerController;
import com.hotelapp.controller.customer.RoomCellController;
import com.hotelapp.model.RoomType;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.ListCell;
import javafx.scene.layout.VBox;

import java.io.IOException;

public class RoomCell extends ListCell<RoomType> {
    private FXMLLoader loader;
    private VBox root;
    private RoomCellController controller;
    private DashboardCustomerController dashboardController;

    public RoomCell(DashboardCustomerController dashboardController) {
        this.dashboardController = dashboardController;
    }

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
                controller.setRoomTypeData(roomType, dashboardController);
                setGraphic(root);
            }
        }
    }
}