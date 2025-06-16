package com.hotelapp.controller.admin;

import com.hotelapp.dao.ReservationDAO;
import com.hotelapp.dao.RoomDAO;
import com.hotelapp.dao.UserDAO;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.chart.*;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;

import java.text.NumberFormat;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class DashboardContentController {

    @FXML private Label totalRevenueLabel;
    @FXML private Label totalReservationsLabel;
    @FXML private Label totalCustomersLabel;
    @FXML private Label totalRoomsLabel;
    @FXML private ProgressBar totalRevenueProgress;
    @FXML private ProgressBar totalReservationsProgress;
    @FXML private ProgressBar newCustomersProgress;
    @FXML private ProgressBar roomsAvailableProgress;
    @FXML private LineChart<String, Number> revenueChart;
    @FXML private PieChart roomTypePieChart;
    @FXML private VBox legendContainer;

    @FXML
    public void initialize() {
        setupInitialUI();
        loadAllData();
    }

    private void setupInitialUI() {
        revenueChart.setAnimated(false);
        revenueChart.setLegendVisible(false);

        roomTypePieChart.setAnimated(false);
        roomTypePieChart.setLegendVisible(false);
        roomTypePieChart.setLabelsVisible(true);
    }

    private void loadAllData() {
        loadStats();
        loadRevenueTrendData();
        loadRoomTypeData();
    }

    private void loadRoomTypeData() {
        Task<ObservableList<PieChart.Data>> task = new Task<>() {
            @Override
            protected ObservableList<PieChart.Data> call() throws Exception {
                Map<String, Integer> roomTypeData = ReservationDAO.getRoomTypeReservationCount();
                ObservableList<PieChart.Data> pieChartData = FXCollections.observableArrayList();

                // --- PERUBAHAN DI SINI ---
                // Kita kembali menggunakan nama tipe kamar saja untuk label di grafik,
                // tanpa menambahkan persentase.
                roomTypeData.forEach((roomType, count) -> {
                    pieChartData.add(new PieChart.Data(roomType, count));
                });

                return pieChartData;
            }
        };

        task.setOnSucceeded(e -> {
            ObservableList<PieChart.Data> pieChartData = task.getValue();
            roomTypePieChart.setData(pieChartData);
            buildCustomLegend(pieChartData);
        });

        new Thread(task).start();
    }

    private void buildCustomLegend(ObservableList<PieChart.Data> pieChartData) {
        legendContainer.getChildren().clear();
        final List<Color> colorPalette = Arrays.asList(
                Color.web("#34CAA5"), Color.web("#E74C3C"), Color.web("#F9A458"),
                Color.web("#A455F1"), Color.web("#5499C7")
        );

        int colorIndex = 0;
        for (PieChart.Data data : pieChartData) {
            Color color = colorPalette.get(colorIndex % colorPalette.size());

            Platform.runLater(() -> {
                if (data.getNode() != null) {
                    data.getNode().setStyle("-fx-pie-color: " + toWebColor(color) + ";");
                }
            });
            // Legenda di bawah tetap menampilkan nama dan jumlah absolut
            legendContainer.getChildren().add(createLegendItem(data.getName(), (int) data.getPieValue(), color));
            colorIndex++;
        }
    }

    // ... sisa kode lainnya tidak ada yang berubah ...
    private void loadStats() {
        Task<Void> task = new Task<>() {
            @Override
            protected Void call() throws Exception {
                double totalRevenue = ReservationDAO.getTotalRevenue();
                int totalReservations = ReservationDAO.getTotalReservations();
                int totalCustomers = UserDAO.getTotalCustomers();
                int totalRooms = RoomDAO.getTotalRooms();
                NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("id", "ID"));

                Platform.runLater(() -> {
                    totalRevenueLabel.setText(currencyFormat.format(totalRevenue));
                    totalReservationsLabel.setText(String.valueOf(totalReservations));
                    totalCustomersLabel.setText(String.valueOf(totalCustomers));
                    totalRoomsLabel.setText(String.valueOf(totalRooms));
                    totalRevenueProgress.setProgress(0.7);
                    totalReservationsProgress.setProgress(0.6);
                    newCustomersProgress.setProgress(0.45);
                    roomsAvailableProgress.setProgress(0.8);
                });
                return null;
            }
        };
        new Thread(task).start();
    }

    private void loadRevenueTrendData() {
        Task<XYChart.Series<String, Number>> task = new Task<>() {
            @Override
            protected XYChart.Series<String, Number> call() {
                Map<String, Double> dailyRevenueData = ReservationDAO.getDailyRevenueTrend(7);
                XYChart.Series<String, Number> series = new XYChart.Series<>();
                dailyRevenueData.entrySet().stream()
                        .sorted(Map.Entry.comparingByKey())
                        .forEach(entry -> series.getData().add(new XYChart.Data<>(entry.getKey(), entry.getValue())));
                return series;
            }
        };

        task.setOnSucceeded(e -> {
            revenueChart.getData().clear();
            revenueChart.getData().add(task.getValue());
        });

        new Thread(task).start();
    }

    private BorderPane createLegendItem(String name, int value, Color color) {
        BorderPane legendItem = new BorderPane();
        HBox nameBox = new HBox(10);
        nameBox.setAlignment(Pos.CENTER_LEFT);
        Circle dot = new Circle(5, color);
        dot.getStyleClass().add("legend-dot");
        Label nameLabel = new Label(name);
        nameLabel.getStyleClass().add("legend-name");
        nameBox.getChildren().addAll(dot, nameLabel);
        Label valueLabel = new Label(String.valueOf(value));
        valueLabel.getStyleClass().add("legend-value");
        legendItem.setLeft(nameBox);
        legendItem.setRight(valueLabel);
        return legendItem;
    }

    private String toWebColor(Color color) {
        return String.format("#%02X%02X%02X",
                (int) (color.getRed() * 255), (int) (color.getGreen() * 255), (int) (color.getBlue() * 255));
    }
}