package com.chargepath.model;

/**
 * Rotanın tek bir parçası (bacak): bir noktadan bir sonrakine sürüş,
 * varışta şarj istasyonuysa orada yapılan şarj.
 *
 *   [from] ---- sürüş (roadKm, energyKwh) ----> [to] (+ varsa şarj)
 */
public final class RouteLeg {

    private final Location from;
    private final Location to;
    private final ChargingStation station;  // varış bir istasyon değilse null
    private final double roadKm;
    private final double energyKwh;
    private final double departSocKwh;
    private final double arrivalSocKwh;
    private final double chargedKwh;
    private final double chargeCost;
    private final double driveHours;
    private final double chargeHours;

    public RouteLeg(Location from, Location to, ChargingStation station, double roadKm, double energyKwh,
                    double departSocKwh, double arrivalSocKwh, double chargedKwh, double chargeCost,
                    double driveHours, double chargeHours) {
        this.from = from;
        this.to = to;
        this.station = station;
        this.roadKm = roadKm;
        this.energyKwh = energyKwh;
        this.departSocKwh = departSocKwh;
        this.arrivalSocKwh = arrivalSocKwh;
        this.chargedKwh = chargedKwh;
        this.chargeCost = chargeCost;
        this.driveHours = driveHours;
        this.chargeHours = chargeHours;
    }

    /** Varış noktası bir şarj istasyonu mu? */
    public boolean endsAtStation() {
        return station != null;
    }

    public Location getFrom() {
        return from;
    }

    public Location getTo() {
        return to;
    }

    public ChargingStation getStation() {
        return station;
    }

    public double getRoadKm() {
        return roadKm;
    }

    public double getEnergyKwh() {
        return energyKwh;
    }

    public double getDepartSocKwh() {
        return departSocKwh;
    }

    public double getArrivalSocKwh() {
        return arrivalSocKwh;
    }

    public double getChargedKwh() {
        return chargedKwh;
    }

    public double getChargeCost() {
        return chargeCost;
    }

    public double getDriveHours() {
        return driveHours;
    }

    public double getChargeHours() {
        return chargeHours;
    }
}
