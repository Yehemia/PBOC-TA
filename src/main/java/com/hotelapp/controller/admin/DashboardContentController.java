package com.hotelapp.controller.admin;

import com.hotelapp.dao.PenaltyDAO;
import com.hotelapp.dao.ReservationDAO;
import com.hotelapp.dao.RoomDAO;
import com.hotelapp.dao.UserDAO;
import com.hotelapp.util.AlertHelper;
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
import java.util.*;

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
    private final List<Color> colorPalette = Arrays.asList(
            Color.web("#5499C7"),
            Color.web("#A455F1"),
            Color.web("#F9A458"),
            Color.web("#E74C3C"),
            Color.web("#34CAA5")
    );

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
        Task<Map<String, Integer>> task = new Task<>() {
            @Override
            protected Map<String, Integer> call() throws Exception {
                return ReservationDAO.getRoomTypeReservationCount();
            }
        };

        task.setOnSucceeded(e -> {
            Map<String, Integer> roomTypeData = task.getValue();
            if (roomTypeData == null || roomTypeData.isEmpty()) {
                roomTypePieChart.setData(FXCollections.observableArrayList());
                legendContainer.getChildren().clear();
                return;
            }

            ObservableList<PieChart.Data> pieChartData = FXCollections.observableArrayList();
            roomTypeData.entrySet().stream()
                    .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                    .forEach(entry -> pieChartData.add(new PieChart.Data(entry.getKey(), entry.getValue())));

            roomTypePieChart.setData(pieChartData);
            applyColorsAndBuildLegend(pieChartData);
        });

        task.setOnFailed(e -> {
            AlertHelper.showError("Gagal Memuat Grafik", "Tidak dapat mengambil data reservasi");
            System.err.println("Failed to load room type data: " + task.getException().getMessage());
        });

        new Thread(task).start();
    }

    private void applyColorsAndBuildLegend(ObservableList<PieChart.Data> pieChartData) {
        legendContainer.getChildren().clear();
        double total = pieChartData.stream().mapToDouble(PieChart.Data::getPieValue).sum();

        int colorIndex = 0;
        for (PieChart.Data data : pieChartData) {
            Color color = colorPalette.get(colorIndex % colorPalette.size());
            String webColor = toWebColor(color);

            data.getNode().setStyle("-fx-pie-color: " + webColor + ";");
            double percentage = (data.getPieValue() / total) * 100;
            String legendLabelText = String.format("%s (%.1f%%)", data.getName(), percentage);
            int count = (int) data.getPieValue();
            legendContainer.getChildren().add(createLegendItem(legendLabelText, count, color));

            colorIndex++;
        }
    }

    private void buildCustomLegend(ObservableList<PieChart.Data> pieChartData) {
        legendContainer.getChildren().clear();
        double total = pieChartData.stream().mapToDouble(PieChart.Data::getPieValue).sum();
        final List<Color> colorPalette = Arrays.asList(
                Color.web("#34CAA5"), Color.web("#E74C3C"), Color.web("#F9A458"),
                Color.web("#A455F1"), Color.web("#5499C7")
        );

        int colorIndex = 0;
        for (PieChart.Data data : pieChartData) {
            double percentage = (data.getPieValue() / total) * 100;
            String legendLabelText = String.format("%s (%.1f%%)", data.getName(), percentage);

            int count = (int) data.getPieValue();
            legendContainer.getChildren().add(createLegendItem(legendLabelText, count, colorPalette.get(colorIndex % colorPalette.size())));
            colorIndex++;
        }
    }

    private void loadStats() {
        Task<Void> task = new Task<>() {
            @Override
            protected Void call() throws Exception {
                double reservationRevenue = ReservationDAO.getTotalRevenue();
                double penaltyRevenue = PenaltyDAO.getTotalPaidPenalties();
                double totalRevenue = reservationRevenue + penaltyRevenue;

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

        task.setOnFailed(e -> {
            AlertHelper.showError("Gagal Memuat Grafik", "Tidak dapat mengambil data tren pendapatan harian.");
            System.err.println("Failed to load revenue trend data: " + task.getException().getMessage());
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
                (int) (color.getRed() * 255),
                (int) (color.getGreen() * 255),
                (int) (color.getBlue() * 255));
    }

}