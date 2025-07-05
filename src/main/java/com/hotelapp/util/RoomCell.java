package com.hotelapp.util;

import com.hotelapp.controller.customer.DashboardCustomerController;
import com.hotelapp.controller.customer.RoomCellController;
import com.hotelapp.model.RoomType;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.ListCell;
import javafx.scene.layout.VBox;

import java.io.IOException;

/**
 * Kelas ini sebenarnya adalah cara alternatif untuk menampilkan item dalam sebuah ListView.
 * Setiap item di list akan digambar menggunakan file FXML terpisah (RoomCell.fxml).
 * Namun, dalam proyek ini, tampaknya pendekatan menggunakan FlowPane dan memuat FXML secara manual
 * di DashboardContentController lebih dominan digunakan.
 * Kelas ini bisa dianggap sebagai sisa dari pendekatan desain yang berbeda atau alternatif.
 */
public class RoomCell extends ListCell<RoomType> {
    private FXMLLoader loader;
    private VBox root;
    private RoomCellController controller;
    private DashboardCustomerController dashboardController;

    public RoomCell(DashboardCustomerController dashboardController) {
        this.dashboardController = dashboardController;
    }

    /**
     * Metode ini dipanggil oleh JavaFX setiap kali sebuah sel di ListView perlu digambar atau diperbarui.
     * @param roomType Objek RoomType untuk baris ini.
     * @param empty true jika baris ini kosong.
     */
    @Override
    protected void updateItem(RoomType roomType, boolean empty) {
        super.updateItem(roomType, empty);

        // Jika baris kosong atau datanya null, jangan tampilkan apa-apa.
        if (empty || roomType == null) {
            setText(null);
            setGraphic(null);
        } else {
            // Jika ini pertama kali sel digambar, muat file FXML-nya.
            if (loader == null) {
                loader = new FXMLLoader(getClass().getResource("/com/hotelapp/fxml/customer/RoomCell.fxml"));
                try {
                    root = loader.load();
                    controller = loader.getController();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            // Jika controller berhasil dimuat, kirim data ke sana.
            if (controller != null) {
                controller.setRoomTypeData(roomType, dashboardController);
                // Tampilkan FXML yang sudah diisi data sebagai konten sel ini.
                setGraphic(root);
            }
        }
    }
}