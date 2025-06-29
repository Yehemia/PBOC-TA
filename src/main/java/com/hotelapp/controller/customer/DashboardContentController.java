package com.hotelapp.controller.customer;

import com.hotelapp.dao.RoomTypeDAO;
import com.hotelapp.model.RoomType;
import com.hotelapp.util.AlertHelper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.layout.FlowPane;
import java.io.IOException;
import java.util.List;

public class DashboardContentController {

    @FXML private FlowPane roomFlowPane;
    @FXML private ProgressIndicator loadingIndicator;

    private DashboardCustomerController dashboardCustomerController;

    @FXML
    public void initialize() {
    }

    public void setDashboardCustomerController(DashboardCustomerController dashboardCustomerController) {
        this.dashboardCustomerController = dashboardCustomerController;
        loadAvailableRoomTypes();
    }

    private void loadAvailableRoomTypes() {
        loadingIndicator.setVisible(true);
        roomFlowPane.setDisable(true);

        Task<List<RoomType>> loadTask = new Task<>() {
            @Override
            protected List<RoomType> call() throws Exception {
                return RoomTypeDAO.getRoomTypesWithAvailability();
            }
        };

        loadTask.setOnSucceeded(e -> {
            roomFlowPane.getChildren().clear();
            List<RoomType> roomTypes = loadTask.getValue();

            for (RoomType rt : roomTypes) {
                if (rt.getAvailableRoomCount() > 0) {
                    try {
                        FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/hotelapp/fxml/customer/RoomCell.fxml"));
                        Node roomCard = loader.load();
                        RoomCellController controller = loader.getController();

                        controller.setRoomTypeData(rt, this.dashboardCustomerController);

                        roomFlowPane.getChildren().add(roomCard);
                    } catch (IOException ioException) {
                        ioException.printStackTrace();
                    }
                }
            }
            loadingIndicator.setVisible(false);
            roomFlowPane.setDisable(false);
        });

        loadTask.setOnFailed(e -> {
            loadingIndicator.setVisible(false);
            roomFlowPane.setDisable(false);
            AlertHelper.showError("Gagal Memuat Kamar", "Terjadi kesalahan saat mengambil daftar kamar dari server.");
            System.err.println("Failed to load available room types: " + loadTask.getException().getMessage());
        });

        new Thread(loadTask).start();
    }
}