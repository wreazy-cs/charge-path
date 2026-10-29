package com.chargepath.model;

/**
 * Rotanın tek bir bölümünü (bacak) temsil eder: bir noktadan sonrakine yapılan
 * sürüş ve varış noktası istasyonsa orada yapılan şarj.
 *
 *   [from] ---- sürüş ----> [to] (+ istasyonsa şarj)
 *
 * Tüm alanlar final tanımlıdır; hesaplanan bacak sonradan değiştirilemez.
 */
public final class RouteLeg {

    private final Location from;
    private final Location to;
    private final ChargingStation station;  // varış noktası istasyon değilse null
    private final double roadKm;
    private final double energyKwh;         // bu bölümde harcanan enerji
    private final double departSocKwh;      // çıkıştaki batarya seviyesi
    private final double arrivalSocKwh;     // varıştaki batarya seviyesi
    private final double chargedKwh;        // istasyonda yüklenen enerji
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

    // Bölümün sonunda şarj istasyonu olup olmadığını döndürür
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
