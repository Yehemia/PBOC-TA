package com.hotelapp.controller.resepsionis;
import com.hotelapp.dao.ReservationDAO;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import javafx.application.Platform;
import javafx.concurrent.Task;

public class ReceptionistDashboardController {

    @FXML private Label reservasiCount;
    @FXML private Label checkInCount;
    @FXML private Label checkOutCount;
    @FXML private Button logoutBtn;
    @FXML private AnchorPane contentPane;
    @FXML private Button checkInButton, checkOutButton, offlineReservationButton, penaltyButton, historyButton;


    @FXML
    public void initialize() {
        loadStatistics();

        checkInButton.setOnAction(e -> loadContent("/com/hotelapp/fxml/resepsionis/CheckInView.fxml"));
        checkOutButton.setOnAction(e -> loadContent("/com/hotelapp/fxml/resepsionis/CheckOutView.fxml"));
        offlineReservationButton.setOnAction(e -> loadContent("/com/hotelapp/fxml/resepsionis/OfflineReservationView.fxml"));
        penaltyButton.setOnAction(e -> loadContent("/com/hotelapp/fxml/resepsionis/PenaltyView.fxml"));
        historyButton.setOnAction(e -> loadContent("/com/hotelapp/fxml/resepsionis/HistoryView.fxml") );
        logoutBtn.setOnAction(e -> performLogout());
    }

    private void loadContent(String fxmlPath) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent view = loader.load();
            contentPane.getChildren().setAll(view);
            AnchorPane.setTopAnchor(view, 0.0);
            AnchorPane.setRightAnchor(view, 0.0);
            AnchorPane.setBottomAnchor(view, 0.0);
            AnchorPane.setLeftAnchor(view, 0.0);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @FXML
    public void loadStatistics() {
        Task<Void> task = new Task<>() {
            @Override
            protected Void call() throws Exception {
                int reservasiHariIni = ReservationDAO.getJumlahReservasiHariIni();
                int checkInHariIni = ReservationDAO.getJumlahCheckInHariIni();
                int checkOutHariIni = ReservationDAO.getJumlahCheckOutHariIni();

                Platform.runLater(() -> {
                    reservasiCount.setText(String.valueOf(reservasiHariIni));
                    checkInCount.setText(String.valueOf(checkInHariIni));
                    checkOutCount.setText(String.valueOf(checkOutHariIni));
                });
                return null;
            }
        };
        new Thread(task).start();
    }

    private void performLogout() {
        System.out.println("Logging out... ");
        try {
            Parent loginRoot = FXMLLoader.load(getClass().getResource("/com/hotelapp/fxml/Login.fxml"));
            Stage stage = (Stage) logoutBtn.getScene().getWindow();
            stage.setScene(new Scene(loginRoot));
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}

