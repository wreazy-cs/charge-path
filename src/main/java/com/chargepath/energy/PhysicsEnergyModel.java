package com.chargepath.energy;

import com.chargepath.model.ElectricVehicle;
import com.chargepath.model.Location;
import com.chargepath.model.TripConditions;

/**
 * Basitleştirilmiş fizik tabanlı enerji modeli.
 *
 *   Toplam = Sürüş + Klima + Yokuş - Rejeneratif kazanç
 *
 * Sürüş : km x tüketim x hız çarpanı x sıcaklık çarpanı
 * Klima : klima gücü (kW) x süre (saat)
 * Yokuş : tırmanışta harcanan, inişte kısmen geri kazanılan potansiyel enerji
 *
 * Katsayılar varsayımdır ve README'de belirtilmiştir.
 */
public class PhysicsEnergyModel implements EnergyModel {

    private static final double GRAVITY = 9.81;              // yerçekimi ivmesi (m/s^2)
    private static final double JOULES_PER_KWH = 3_600_000;  // 1 kWh = 3,6 milyon joule
    private static final double REFERENCE_SPEED_KMH = 90;    // katalog tüketiminin geçerli olduğu hız (varsayım)
    private static final double DRIVETRAIN_EFFICIENCY = 0.90; // motor ve aktarma verimi
    private static final double REGEN_EFFICIENCY = 0.60;     // inişte geri kazanılan enerji oranı
    private static final double PAYLOAD_KG = 150;            // sürücü ve yük (varsayım)

    @Override
    public double legEnergyKwh(ElectricVehicle vehicle, Location from, Location to,
                               double roadKm, TripConditions conditions) {
        if (roadKm < 0) throw new IllegalArgumentException("Mesafe negatif olamaz");

        double drive = drivingKwhPerKm(vehicle, conditions) * roadKm;
        // Klima, sürüş süresi (yol / hız) boyunca çalışır
        double climate = climatePowerKw(conditions) * (roadKm / conditions.getSpeedKmh());
        // Pozitif yükseklik farkı tırmanış, negatif fark iniş anlamına gelir
        double slope = slopeKwh(vehicle, to.getElevationM() - from.getElevationM());

        // Uzun inişlerde sonuç negatif olabilir; bataryanın inişte dolması
        // modellenmediği için alt sınır 0 kabul edilir.
        return Math.max(0, drive + climate + slope);
    }

    @Override
    public double rangeKm(ElectricVehicle vehicle, double availableKwh, TripConditions conditions) {
        if (availableKwh <= 0) return 0;
        // Menzil = mevcut enerji / km başına enerji
        double climatePerKm = climatePowerKw(conditions) / conditions.getSpeedKmh();
        double perKm = drivingKwhPerKm(vehicle, conditions) + climatePerKm;
        return availableKwh / perKm;
    }

    /**
     * Düz yolda 1 km için sürüş enerjisi (kWh).
     * Hız çarpanı = 0,6 + 0,4 x hava direnci x (hız / 90)^2
     * 0,6 sabit kayıpları (yuvarlanma vb.), 0,4 hava direncini temsil eder.
     * Hava direnci hızın karesiyle arttığı için tüketim yüksek hızlarda belirgin şekilde artar.
     */
    double drivingKwhPerKm(ElectricVehicle vehicle, TripConditions conditions) {
        double ratio = conditions.getSpeedKmh() / REFERENCE_SPEED_KMH;
        double speedFactor = 0.6 + 0.4 * vehicle.getDragFactor() * ratio * ratio;
        // Wh -> kWh dönüşümü için 1000'e bölünür
        return vehicle.getConsumptionWhPerKm() / 1000.0 * speedFactor * temperatureFactor(conditions.getTemperatureC());
    }

    /**
     * Sıcaklığın tüketime etkisi.
     * Varsayım: 20 °C altında ve 30 °C üstünde derece başına %0,5 ek tüketim
     * (soğukta en fazla %25).
     */
    double temperatureFactor(double temperatureC) {
        if (temperatureC < 20) return Math.min(1.25, 1 + (20 - temperatureC) * 0.005);
        if (temperatureC > 30) return 1 + (temperatureC - 30) * 0.005;
        return 1.0;
    }

    /**
     * Klimanın çektiği güç (kW). Isıtma, soğutmaya göre daha fazla enerji gerektirir.
     * Varsayım: 18 °C altında 1 kW + derece başına 0,1 kW (en fazla 4 kW),
     * 24 °C üstünde 0,5 kW + derece başına 0,1 kW (en fazla 3 kW),
     * aradaki sıcaklıklarda yalnızca fan (0,3 kW).
     */
    double climatePowerKw(TripConditions conditions) {
        if (!conditions.isClimateOn()) return 0;
        double t = conditions.getTemperatureC();
        if (t < 18) return Math.min(4.0, 1.0 + 0.1 * (18 - t));
        if (t > 24) return Math.min(3.0, 0.5 + 0.1 * (t - 24));
        return 0.3;
    }

    // Potansiyel enerji (m x g x h), joule'dan kWh'e çevrilir
    double slopeKwh(ElectricVehicle vehicle, double deltaHeightM) {
        double massKg = vehicle.getMassKg() + PAYLOAD_KG;
        double potentialKwh = massKg * GRAVITY * Math.abs(deltaHeightM) / JOULES_PER_KWH;
        if (deltaHeightM >= 0) {
            // Tırmanış: aktarma kayıpları nedeniyle daha fazla enerji harcanır
            return potentialKwh / DRIVETRAIN_EFFICIENCY;
        }
        // İniş: rejeneratif frenle enerjinin %60'ı geri kazanılır (negatif değer kazançtır)
        return -potentialKwh * REGEN_EFFICIENCY;
    }
}
