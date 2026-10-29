package com.chargepath.data;

import com.chargepath.model.Route;
import com.chargepath.model.RouteLeg;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Yolculuk geçmişine kaydedilen özet bilgi.
 * trip_history.json dosyasına yazıldığı için program kapatıldığında kaybolmaz (kalıcılık).
 */
public class TripRecord {

    // Tarih biçimi: "29.10.2026 14:30"
    private static final DateTimeFormatter FORMAT = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");

    // Gson dosyadan okurken alanları doldurduğu için final tanımlanmamıştır
    private String date;
    private String vehicle;
    private String from;
    private String to;
    private String strategy;
    private double distanceKm;
    private double energyKwh;
    private double cost;
    private double hours;
    private int stopCount;
    private List<String> stops;

    public TripRecord(String date, String vehicle, String from, String to, String strategy, double distanceKm,
                      double energyKwh, double cost, double hours, int stopCount, List<String> stops) {
        this.date = date;
        this.vehicle = vehicle;
        this.from = from;
        this.to = to;
        this.strategy = strategy;
        this.distanceKm = distanceKm;
        this.energyKwh = energyKwh;
        this.cost = cost;
        this.hours = hours;
        this.stopCount = stopCount;
        this.stops = new ArrayList<>(stops);
    }

    /**
     * Statik üretici metot (static factory method): rotadan kayıt oluşturur.
     * 11 parametrelik kurucu yerine tek bir Route nesnesi yeterlidir.
     * LocalDate.now() da aynı yaklaşımı kullanır.
     */
    public static TripRecord from(Route route) {
        // Şarj yapılan istasyonların adları listelenir
        List<String> stopNames = new ArrayList<>();
        for (RouteLeg leg : route.getLegs()) {
            if (leg.endsAtStation()) stopNames.add(leg.getStation().getId() + " " + leg.getStation().getName());
        }
        return new TripRecord(
                LocalDateTime.now().format(FORMAT),
                route.getVehicle().getDisplayName(),
                route.getStart().getName(),
                route.getDestination().getName(),
                route.getStrategyName(),
                round(route.getTotalDistanceKm()),
                round(route.getTotalEnergyKwh()),
                round(route.getTotalCost()),
                round(route.getTotalHours()),
                route.getStopCount(),
                stopNames);
    }

    // Virgülden sonra 2 basamağa yuvarlar (12,3456 -> 12,35)
    private static double round(double value) {
        return Math.round(value * 100.0) / 100.0;
    }

    public String getDate() {
        return date;
    }

    public String getVehicle() {
        return vehicle;
    }

    public String getFrom() {
        return from;
    }

    public String getTo() {
        return to;
    }

    public String getStrategy() {
        return strategy;
    }

    public double getDistanceKm() {
        return distanceKm;
    }

    public double getEnergyKwh() {
        return energyKwh;
    }

    public double getCost() {
        return cost;
    }

    public double getHours() {
        return hours;
    }

    public int getStopCount() {
        return stopCount;
    }

    // Listesi olmayan eski kayıtlarda null yerine boş liste döner
    public List<String> getStops() {
        return stops == null ? List.of() : List.copyOf(stops);
    }
}
