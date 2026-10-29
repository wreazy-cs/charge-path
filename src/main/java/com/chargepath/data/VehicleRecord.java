package com.chargepath.data;

/**
 * vehicles_fallback.json dosyasındaki bir araç kaydının Java karşılığı.
 * DTO (Data Transfer Object): iş kuralı içermez, yalnızca veri taşır.
 * Gson bu sınıfı doldurur, VehicleFactory araç nesnesine dönüştürür.
 */
public class VehicleRecord {
    public String id;
    public String brand;
    public String model;
    public String type;        // "SEDAN" ya da "SUV"
    public double batteryKwh;
    public double consumptionWhPerKm;
    public double massKg;
    public double maxDcKw;
    public double maxAcKw;
    public String notes;
}
