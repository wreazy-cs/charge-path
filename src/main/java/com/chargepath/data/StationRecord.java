package com.chargepath.data;

// stations_fallback.json dosyasındaki bir istasyon kaydının Java karşılığı (DTO).
// Yalnızca veri taşır; istasyon nesnesine StationFactory tarafından dönüştürülür.
public class StationRecord {
    public String id;
    public String name;
    public String operator;
    public String city;
    public double lat;
    public double lon;
    public double elevationM;
    public String type;        // "AC" ya da "DC"
    public double powerKw;
    public double pricePerKwh;
    public String notes;
}
