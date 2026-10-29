package com.chargepath.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Planlanan rotanın tamamını, sıralı bacak listesi olarak tutar.
 * Toplam değerler ayrıca saklanmaz, her çağrıda bacaklardan hesaplanır;
 * böylece veriler her zaman tutarlı kalır.
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
        // Savunmacı kopya: dışarıdaki liste değişse de rota etkilenmez
        this.legs = new ArrayList<>(legs);
    }

    // Salt okunur liste döner; rota dışarıdan değiştirilemez (kapsülleme)
    public List<RouteLeg> getLegs() {
        return Collections.unmodifiableList(legs);
    }

    public Location getStart() {
        return legs.get(0).getFrom();
    }

    public Location getDestination() {
        return legs.get(legs.size() - 1).getTo();
    }

    // Aşağıdaki toplam metotları tüm bacakları dolaşarak ilgili değeri toplar

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

    // Toplam süre = sürüş süresi + şarj süresi
    public double getTotalHours() {
        return getTotalDriveHours() + getTotalChargeHours();
    }

    // Yol boyunca yapılan şarj duraklarının sayısı
    public int getStopCount() {
        int count = 0;
        for (RouteLeg leg : legs) {
            if (leg.endsAtStation()) count++;
        }
        return count;
    }

    // Varıştaki batarya seviyesi, son bacağın varış değeridir
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
