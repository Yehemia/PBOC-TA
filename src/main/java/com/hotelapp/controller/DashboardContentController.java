package com.hotelapp.controller;

import com.hotelapp.model.Room;
import com.hotelapp.service.RoomService;
import com.hotelapp.util.RoomCell;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.ListView;

public class DashboardContentController {

    @FXML
    private ListView<Room> roomList;

    // Referensi ke DashboardCustomerController yang akan diinject
    private DashboardCustomerController dashboardCustomerController;

    @FXML
    public void initialize() {
        // Ambil daftar kamar yang tersedia
        ObservableList<Room> availableRooms = FXCollections.observableArrayList(RoomService.getAvailableRooms());
        roomList.setItems(availableRooms);
        // Jangan set cell factory di sini karena dashboardCustomerController mungkin belum ada!
        System.out.println("DashboardContentController.initialize(): ListView terisi dengan " + availableRooms.size() + " kamar.");
    }

    public void setDashboardCustomerController(DashboardCustomerController dashboardCustomerController) {
        this.dashboardCustomerController = dashboardCustomerController;
        // Setelah mendapatkan referensi, set cell factory sehingga tiap RoomCell dibuat dengan referensi yang valid
        roomList.setCellFactory(listView -> new RoomCell(dashboardCustomerController));
        System.out.println("DashboardContentController.setDashboardCustomerController(): Referensi dashboard diset.");
    }
}