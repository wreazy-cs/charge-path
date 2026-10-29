package com.chargepath.data;

import com.chargepath.model.Route;
import com.chargepath.model.RouteLeg;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Yolculuk geçmişine kaydedilen özet. trip_history.json dosyasına yazılır,
 * program kapanıp açılsa da kaybolmaz (kalıcılık).
 */
public class TripRecord {

    private static final DateTimeFormatter FORMAT = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");

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
     * Statik üretici metot (static factory method): bir rotadan kayıt üretir.
     * new ile tek tek alan doldurmak yerine "Route ver, kaydı al" der.
     */
    public static TripRecord from(Route route) {
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

    public List<String> getStops() {
        return stops == null ? List.of() : List.copyOf(stops);
    }
}
