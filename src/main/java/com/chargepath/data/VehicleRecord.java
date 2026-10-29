package com.chargepath.data;

/**
 * JSON dosyasındaki bir araç satırının birebir karşılığı (DTO: veri taşıma nesnesi).
 * Gson, JSON'daki alan adlarını bu sınıftaki alan adlarıyla eşleştirir.
 * İş kuralı içermez, sadece veriyi taşır. Gerçek nesneye VehicleFactory çevirir.
 */
public class VehicleRecord {
    public String id;
    public String brand;
    public String model;
    public String type;
    public double batteryKwh;
    public double consumptionWhPerKm;
    public double massKg;
    public double maxDcKw;
    public double maxAcKw;
    public String notes;
}
