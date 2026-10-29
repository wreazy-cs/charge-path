package com.chargepath.data;

/** JSON dosyasındaki bir istasyon satırının birebir karşılığı (DTO). */
public class StationRecord {
    public String id;
    public String name;
    public String operator;
    public String city;
    public double lat;
    public double lon;
    public double elevationM;
    public String type;
    public double powerKw;
    public double pricePerKwh;
    public String notes;
}
