package com.chargepath.model;

/**
 * Tüm elektrikli araçların ortak şablonu (soyut sınıf).
 *
 * abstract: Bu sınıftan doğrudan nesne üretilemez ("new ElectricVehicle" yok).
 * Sadece Sedan ve Suv gibi alt sınıflar üzerinden nesne üretilir.
 *
 * Kapsülleme: batarya seviyesi (socKwh) private. Dışarıdan sadece
 * consume() ve charge() kapılarıyla değişir, o kapılar da kuralları korur:
 * seviye asla 0'ın altına ya da kapasitenin üstüne çıkmaz.
 */
public abstract class ElectricVehicle {

    private final String id;
    private final String brand;
    private final String model;
    private final double batteryKwh;          // kullanılabilir batarya kapasitesi
    private final double consumptionWhPerKm;  // ortalama tüketim (Wh/km)
    private final double massKg;              // boş ağırlık
    private final double maxDcKw;             // DC hızlı şarjda kabul edebildiği en yüksek güç
    private final double maxAcKw;             // AC şarjda kabul edebildiği en yüksek güç
    private double socKwh;                    // anlık batarya seviyesi (State of Charge)

    protected ElectricVehicle(String id, String brand, String model, double batteryKwh,
                              double consumptionWhPerKm, double massKg, double maxDcKw, double maxAcKw) {
        if (id == null || id.isBlank()) throw new IllegalArgumentException("Araç kimliği boş olamaz");
        if (batteryKwh <= 0) throw new IllegalArgumentException("Batarya kapasitesi 0'dan büyük olmalı");
        if (consumptionWhPerKm <= 0) throw new IllegalArgumentException("Tüketim 0'dan büyük olmalı");
        if (massKg <= 0) throw new IllegalArgumentException("Ağırlık 0'dan büyük olmalı");
        if (maxDcKw <= 0 || maxAcKw <= 0) throw new IllegalArgumentException("Şarj güçleri 0'dan büyük olmalı");
        this.id = id;
        this.brand = brand;
        this.model = model;
        this.batteryKwh = batteryKwh;
        this.consumptionWhPerKm = consumptionWhPerKm;
        this.massKg = massKg;
        this.maxDcKw = maxDcKw;
        this.maxAcKw = maxAcKw;
        this.socKwh = batteryKwh; // yeni araç dolu başlar
    }

    /**
     * Hava direnci katsayısı (çok biçimlilik): her alt sınıf kendi değerini verir.
     * Sedan alçak ve aerodinamik, SUV daha yüksek ve geniş.
     */
    public abstract double getDragFactor();

    /** Araç tipinin okunabilir adı ("Sedan", "SUV"). */
    public abstract String getTypeName();

    /** Bataryadan enerji harcar. Yetmezse hata verir, seviye eksiye düşmez. */
    public void consume(double kwh) {
        if (kwh < 0) throw new IllegalArgumentException("Tüketim negatif olamaz");
        if (kwh > socKwh) throw new IllegalStateException("Yetersiz batarya: " + socKwh + " kWh var, " + kwh + " kWh istendi");
        socKwh -= kwh;
    }

    /** Bataryayı şarj eder. Kapasiteyi aşan kısım bataryaya girmez. */
    public void charge(double kwh) {
        if (kwh < 0) throw new IllegalArgumentException("Şarj miktarı negatif olamaz");
        socKwh = Math.min(batteryKwh, socKwh + kwh);
    }

    /** Batarya seviyesini yüzde olarak ayarlar (senaryo başlangıcı için). */
    public void setSocPercent(double percent) {
        if (percent < 0 || percent > 100) throw new IllegalArgumentException("Yüzde 0 ile 100 arasında olmalı");
        socKwh = batteryKwh * percent / 100.0;
    }

    public double getSocPercent() {
        return socKwh / batteryKwh * 100.0;
    }

    public double getSocKwh() {
        return socKwh;
    }

    public String getId() {
        return id;
    }

    public String getBrand() {
        return brand;
    }

    public String getModel() {
        return model;
    }

    public String getDisplayName() {
        return brand + " " + model;
    }

    public double getBatteryKwh() {
        return batteryKwh;
    }

    public double getConsumptionWhPerKm() {
        return consumptionWhPerKm;
    }

    public double getMassKg() {
        return massKg;
    }

    public double getMaxDcKw() {
        return maxDcKw;
    }

    public double getMaxAcKw() {
        return maxAcKw;
    }

    @Override
    public String toString() {
        return String.format("%s (%s, %.0f kWh, %.0f Wh/km)", getDisplayName(), getTypeName(), batteryKwh, consumptionWhPerKm);
    }
}
