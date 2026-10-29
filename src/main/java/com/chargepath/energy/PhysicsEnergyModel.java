package com.chargepath.energy;

import com.chargepath.model.ElectricVehicle;
import com.chargepath.model.Location;
import com.chargepath.model.TripConditions;

/**
 * Basitleştirilmiş fizik tabanlı enerji modeli.
 *
 *   Toplam = Sürüş + Klima + Yokuş - Rejeneratif kazanç
 *
 *   Sürüş  = km x tüketim x hızÇarpanı x sıcaklıkÇarpanı
 *            hızÇarpanı = 0.6 + 0.4 x hava direnci x (hız / 90)^2
 *            (0.6 kısmı yuvarlanma ve sabit kayıplar, 0.4 kısmı hava direnci;
 *             hava direnci hızın karesiyle artar)
 *   Klima  = klima gücü (kW) x süre (saat)
 *   Yokuş  = m x g x Δh / 3.6e6 / motor verimi            (Δh > 0, tırmanış)
 *   Rejen  = m x g x |Δh| / 3.6e6 x geri kazanım oranı   (Δh < 0, iniş)
 *
 * Katsayılar varsayımdır ve README'de açıkça belirtilmiştir.
 */
public class PhysicsEnergyModel implements EnergyModel {

    private static final double GRAVITY = 9.81;              // m/s^2
    private static final double JOULES_PER_KWH = 3_600_000;  // 1 kWh = 3.6 milyon joule
    private static final double REFERENCE_SPEED_KMH = 90;    // katalog tüketiminin karşılık geldiği hız (varsayım)
    private static final double DRIVETRAIN_EFFICIENCY = 0.90;
    private static final double REGEN_EFFICIENCY = 0.60;     // inişte geri kazanılan enerji oranı
    private static final double PAYLOAD_KG = 150;            // sürücü + yük (varsayım)

    @Override
    public double legEnergyKwh(ElectricVehicle vehicle, Location from, Location to,
                               double roadKm, TripConditions conditions) {
        if (roadKm < 0) throw new IllegalArgumentException("Mesafe negatif olamaz");

        double drive = drivingKwhPerKm(vehicle, conditions) * roadKm;
        double climate = climatePowerKw(conditions) * (roadKm / conditions.getSpeedKmh());
        double slope = slopeKwh(vehicle, to.getElevationM() - from.getElevationM());

        // Uzun bir inişte rejen sürüşten fazla çıkabilir; bu bacakta bataryanın
        // dolduğunu modellemiyoruz, en az 0 kabul ediyoruz (güvenli tarafta kalmak için).
        return Math.max(0, drive + climate + slope);
    }

    @Override
    public double rangeKm(ElectricVehicle vehicle, double availableKwh, TripConditions conditions) {
        if (availableKwh <= 0) return 0;
        double climatePerKm = climatePowerKw(conditions) / conditions.getSpeedKmh();
        double perKm = drivingKwhPerKm(vehicle, conditions) + climatePerKm;
        return availableKwh / perKm;
    }

    /** Düz yolda 1 km için sürüş enerjisi (kWh). */
    double drivingKwhPerKm(ElectricVehicle vehicle, TripConditions conditions) {
        double ratio = conditions.getSpeedKmh() / REFERENCE_SPEED_KMH;
        double speedFactor = 0.6 + 0.4 * vehicle.getDragFactor() * ratio * ratio;
        return vehicle.getConsumptionWhPerKm() / 1000.0 * speedFactor * temperatureFactor(conditions.getTemperatureC());
    }

    /**
     * Soğukta batarya kimyası yavaşlar, lastik ve hava daha "sert" olur.
     * Varsayım: 20 °C altında her derece %0.5 ek tüketim (en fazla %25),
     * 30 °C üstünde her derece %0.5 ek tüketim.
     */
    double temperatureFactor(double temperatureC) {
        if (temperatureC < 20) return Math.min(1.25, 1 + (20 - temperatureC) * 0.005);
        if (temperatureC > 30) return 1 + (temperatureC - 30) * 0.005;
        return 1.0;
    }

    /**
     * Klimanın çektiği güç (kW). Isıtma soğutmadan daha fazla enerji ister.
     * Varsayım: 18 °C altında ısıtma 1.0 kW + derece başına 0.1 kW (en fazla 4 kW),
     * 24 °C üstünde soğutma 0.5 kW + derece başına 0.1 kW (en fazla 3 kW),
     * arada sadece fan 0.3 kW.
     */
    double climatePowerKw(TripConditions conditions) {
        if (!conditions.isClimateOn()) return 0;
        double t = conditions.getTemperatureC();
        if (t < 18) return Math.min(4.0, 1.0 + 0.1 * (18 - t));
        if (t > 24) return Math.min(3.0, 0.5 + 0.1 * (t - 24));
        return 0.3;
    }

    /** Yükseklik farkının enerjisi: tırmanışta pozitif, inişte (rejen) negatif. */
    double slopeKwh(ElectricVehicle vehicle, double deltaHeightM) {
        double massKg = vehicle.getMassKg() + PAYLOAD_KG;
        double potentialKwh = massKg * GRAVITY * Math.abs(deltaHeightM) / JOULES_PER_KWH;
        if (deltaHeightM >= 0) {
            return potentialKwh / DRIVETRAIN_EFFICIENCY;
        }
        return -potentialKwh * REGEN_EFFICIENCY;
    }
}
