package com.chargepath.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Planlanmış rotanın tamamı: bacakların sıralı listesi ve toplamlar.
 * Toplamlar saklanmaz, her seferinde bacaklardan hesaplanır
 * (tek doğruluk kaynağı: bacak listesi).
 */
public final class Route {

    private final ElectricVehicle vehicle;
    private final TripConditions conditions;
    private final String strategyName;
    private final List<RouteLeg> legs;

    public Route(ElectricVehicle vehicle, TripConditions conditions, String strategyName, List<RouteLeg> legs) {
        if (legs == null || legs.isEmpty()) throw new IllegalArgumentException("Rota en az bir bacak içermeli");
        this.vehicle = vehicle;
        this.conditions = conditions;
        this.strategyName = strategyName;
        this.legs = new ArrayList<>(legs); // savunmacı kopya: dışarıdaki liste değişse de rota değişmez
    }

    /** Dışarıya salt okunur liste verilir, rota dışarıdan bozulamaz (kapsülleme). */
    public List<RouteLeg> getLegs() {
        return Collections.unmodifiableList(legs);
    }

    public Location getStart() {
        return legs.get(0).getFrom();
    }

    public Location getDestination() {
        return legs.get(legs.size() - 1).getTo();
    }

    public double getTotalDistanceKm() {
        double sum = 0;
        for (RouteLeg leg : legs) sum += leg.getRoadKm();
        return sum;
    }

    public double getTotalEnergyKwh() {
        double sum = 0;
        for (RouteLeg leg : legs) sum += leg.getEnergyKwh();
        return sum;
    }

    public double getTotalChargedKwh() {
        double sum = 0;
        for (RouteLeg leg : legs) sum += leg.getChargedKwh();
        return sum;
    }

    public double getTotalCost() {
        double sum = 0;
        for (RouteLeg leg : legs) sum += leg.getChargeCost();
        return sum;
    }

    public double getTotalDriveHours() {
        double sum = 0;
        for (RouteLeg leg : legs) sum += leg.getDriveHours();
        return sum;
    }

    public double getTotalChargeHours() {
        double sum = 0;
        for (RouteLeg leg : legs) sum += leg.getChargeHours();
        return sum;
    }

    public double getTotalHours() {
        return getTotalDriveHours() + getTotalChargeHours();
    }

    /** Yolda kaç kez şarj için durulduğu. */
    public int getStopCount() {
        int count = 0;
        for (RouteLeg leg : legs) {
            if (leg.endsAtStation()) count++;
        }
        return count;
    }

    public double getFinalSocKwh() {
        return legs.get(legs.size() - 1).getArrivalSocKwh();
    }

    public ElectricVehicle getVehicle() {
        return vehicle;
    }

    public TripConditions getConditions() {
        return conditions;
    }

    public String getStrategyName() {
        return strategyName;
    }
}
