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

/**
 * Controller untuk konten utama dasbor admin.
 * Menampilkan statistik, grafik, dan data visual lainnya.
 */
public class DashboardContentController {

    // @FXML menghubungkan variabel ke komponen FXML.
    @FXML private Label totalRevenueLabel, totalReservationsLabel, totalCustomersLabel, totalRoomsLabel;
    @FXML private ProgressBar totalRevenueProgress, totalReservationsProgress, newCustomersProgress, roomsAvailableProgress;
    @FXML private LineChart<String, Number> revenueChart; // Grafik garis untuk tren pendapatan.
    @FXML private PieChart roomTypePieChart; // Diagram lingkaran untuk popularitas tipe kamar.
    @FXML private VBox legendContainer; // Kontainer untuk legenda kustom PieChart.

    // Palet warna yang akan digunakan untuk grafik.
    private final List<Color> colorPalette = Arrays.asList(
            Color.web("#5499C7"), Color.web("#A455F1"), Color.web("#F9A458"),
            Color.web("#E74C3C"), Color.web("#34CAA5")
    );

    /**
     * Inisialisasi awal.
     */
    @FXML
    public void initialize() {
        setupInitialUI();
        loadAllData();
    }

    /**
     * Pengaturan awal untuk tampilan grafik.
     */
    private void setupInitialUI() {
        // Nonaktifkan animasi agar grafik langsung muncul.
        revenueChart.setAnimated(false);
        revenueChart.setLegendVisible(false); // Sembunyikan legenda default.

        roomTypePieChart.setAnimated(false);
        roomTypePieChart.setLegendVisible(false); // Sembunyikan legenda default.
        roomTypePieChart.setLabelsVisible(true); // Tampilkan label langsung di potongan pie.
    }

    /**
     * Memanggil semua metode untuk memuat data.
     */
    private void loadAllData() {
        loadStats();
        loadRevenueTrendData();
        loadRoomTypeData();
    }

    /**
     * Memuat data untuk PieChart (popularitas tipe kamar).
     */
    private void loadRoomTypeData() {
        // Menggunakan Task untuk menjalankan query database di background thread.
        Task<Map<String, Integer>> task = new Task<>() {
            @Override
            protected Map<String, Integer> call() throws Exception {
                // Ambil data jumlah reservasi per tipe kamar.
                return ReservationDAO.getRoomTypeReservationCount();
            }
        };

        // Setelah task berhasil, update UI di JavaFX Application Thread.
        task.setOnSucceeded(e -> {
            Map<String, Integer> roomTypeData = task.getValue();
            if (roomTypeData == null || roomTypeData.isEmpty()) {
                roomTypePieChart.setData(FXCollections.observableArrayList());
                legendContainer.getChildren().clear();
                return;
            }

            ObservableList<PieChart.Data> pieChartData = FXCollections.observableArrayList();
            // Urutkan data dari yang paling populer.
            roomTypeData.entrySet().stream()
                    .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                    .forEach(entry -> pieChartData.add(new PieChart.Data(entry.getKey(), entry.getValue())));

            roomTypePieChart.setData(pieChartData);
            applyColorsAndBuildLegend(pieChartData); // Beri warna dan buat legenda.
        });

        task.setOnFailed(e -> {
            AlertHelper.showError("Gagal Memuat Grafik", "Tidak dapat mengambil data reservasi");
            System.err.println("Failed to load room type data: " + task.getException().getMessage());
        });

        new Thread(task).start(); // Jalankan task.
    }

    /**
     * Memberi warna pada PieChart dan membuat legenda kustom.
     * @param pieChartData Data yang akan ditampilkan.
     */
    private void applyColorsAndBuildLegend(ObservableList<PieChart.Data> pieChartData) {
        legendContainer.getChildren().clear();
        double total = pieChartData.stream().mapToDouble(PieChart.Data::getPieValue).sum();

        int colorIndex = 0;
        for (PieChart.Data data : pieChartData) {
            // Ambil warna dari palet secara bergiliran.
            Color color = colorPalette.get(colorIndex % colorPalette.size());
            String webColor = toWebColor(color);

            // Terapkan warna ke potongan pie.
            data.getNode().setStyle("-fx-pie-color: " + webColor + ";");

            // Buat teks untuk legenda, termasuk persentase.
            double percentage = (data.getPieValue() / total) * 100;
            String legendLabelText = String.format("%s (%.1f%%)", data.getName(), percentage);
            int count = (int) data.getPieValue();

            // Tambahkan item legenda ke VBox.
            legendContainer.getChildren().add(createLegendItem(legendLabelText, count, color));

            colorIndex++;
        }
    }

    /**
     * Memuat data statistik utama (total revenue, reservasi, dll).
     */
    private void loadStats() {
        Task<Void> task = new Task<>() {
            @Override
            protected Void call() throws Exception {
                // Ambil semua data statistik dari berbagai DAO.
                double reservationRevenue = ReservationDAO.getTotalRevenue();
                double penaltyRevenue = PenaltyDAO.getTotalPaidPenalties();
                double totalRevenue = reservationRevenue + penaltyRevenue;
                int totalReservations = ReservationDAO.getTotalReservations();
                int totalCustomers = UserDAO.getTotalCustomers();
                int totalRooms = RoomDAO.getTotalRooms();
                NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("id", "ID"));

                // Update UI di JavaFX thread.
                Platform.runLater(() -> {
                    totalRevenueLabel.setText(currencyFormat.format(totalRevenue));
                    totalReservationsLabel.setText(String.valueOf(totalReservations));
                    totalCustomersLabel.setText(String.valueOf(totalCustomers));
                    totalRoomsLabel.setText(String.valueOf(totalRooms));
                    // Atur progress bar (nilai statis untuk visual).
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

    /**
     * Memuat data untuk grafik tren pendapatan harian.
     */
    private void loadRevenueTrendData() {
        Task<XYChart.Series<String, Number>> task = new Task<>() {
            @Override
            protected XYChart.Series<String, Number> call() {
                // Ambil data pendapatan 7 hari terakhir.
                Map<String, Double> dailyRevenueData = ReservationDAO.getDailyRevenueTrend(7);
                XYChart.Series<String, Number> series = new XYChart.Series<>();
                // Urutkan berdasarkan tanggal dan tambahkan ke series grafik.
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

    /**
     * Membuat satu baris item untuk legenda kustom.
     * @param name Nama item (e.g., "Deluxe (25.0%)").
     * @param value Jumlah reservasi.
     * @param color Warna untuk lingkaran penanda.
     * @return BorderPane yang berisi item legenda.
     */
    private BorderPane createLegendItem(String name, int value, Color color) {
        BorderPane legendItem = new BorderPane();
        HBox nameBox = new HBox(10);
        nameBox.setAlignment(Pos.CENTER_LEFT);

        Circle dot = new Circle(5, color); // Lingkaran kecil berwarna.
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

    /**
     * Mengubah objek Color JavaFX menjadi format string web (#RRGGBB).
     */
    private String toWebColor(Color color) {
        return String.format("#%02X%02X%02X",
                (int) (color.getRed() * 255),
                (int) (color.getGreen() * 255),
                (int) (color.getBlue() * 255));
    }
}