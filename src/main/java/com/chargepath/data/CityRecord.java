package com.chargepath.data;

// cities.json dosyasındaki bir şehir kaydının Java karşılığı (DTO).
// Gson, JSON alan adlarını bu sınıftaki alan adlarıyla eşleştirir.
public class CityRecord {
    public String id;
    public String name;
    public double lat;
    public double lon;
    public double elevationM;
    public String notes;
}
