package com.chargepath.model;

import java.util.Objects;

/**
 * Haritadaki bir nokta: şehir merkezi ya da istasyonun yeri.
 *
 * Değişmez (immutable) sınıf: tüm alanlar private final, setter yok.
 * Bir kez doğan konum bir daha değişmez, bu yüzden güvenle paylaşılır.
 */
public final class Location {

    private static final double EARTH_RADIUS_KM = 6371.0;

    private final String name;
    private final double latitude;   // enlem (kuzey-güney), -90 ile 90 arası
    private final double longitude;  // boylam (doğu-batı), -180 ile 180 arası
    private final double elevationM; // deniz seviyesinden yükseklik (metre)

    public Location(String name, double latitude, double longitude, double elevationM) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Konum adı boş olamaz");
        }
        if (latitude < -90 || latitude > 90) {
            throw new IllegalArgumentException("Enlem -90 ile 90 arasında olmalı: " + latitude);
        }
        if (longitude < -180 || longitude > 180) {
            throw new IllegalArgumentException("Boylam -180 ile 180 arasında olmalı: " + longitude);
        }
        this.name = name;
        this.latitude = latitude;
        this.longitude = longitude;
        this.elevationM = elevationM;
    }

    /**
     * İki nokta arasındaki kuş uçuşu mesafe (km). Haversine formülü:
     * Dünya'yı küre kabul eder ve yay uzunluğunu hesaplar.
     */
    public double distanceKmTo(Location other) {
        double lat1 = Math.toRadians(latitude);
        double lat2 = Math.toRadians(other.latitude);
        double dLat = lat2 - lat1;
        double dLon = Math.toRadians(other.longitude - longitude);

        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(lat1) * Math.cos(lat2) * Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return EARTH_RADIUS_KM * c;
    }

    public String getName() {
        return name;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public double getElevationM() {
        return elevationM;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Location other)) return false;
        return Double.compare(latitude, other.latitude) == 0
                && Double.compare(longitude, other.longitude) == 0
                && name.equals(other.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, latitude, longitude);
    }

    @Override
    public String toString() {
        return name;
    }
}
