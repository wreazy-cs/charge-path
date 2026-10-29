package com.chargepath.model;

import java.util.Objects;

/**
 * Haritadaki bir noktayı (şehir merkezi ya da şarj istasyonu) temsil eder.
 *
 * Tüm alanlar private final tanımlıdır ve setter bulunmaz. Nesne oluşturulduktan
 * sonra değiştirilemez (immutable), bu nedenle güvenle paylaşılabilir.
 */
public final class Location {

    // Dünya'nın ortalama yarıçapı (km)
    private static final double EARTH_RADIUS_KM = 6371.0;

    private final String name;
    private final double latitude;   // enlem, -90 ile 90 arası
    private final double longitude;  // boylam, -180 ile 180 arası
    private final double elevationM; // deniz seviyesinden yükseklik (m), eğim hesabında kullanılır

    public Location(String name, double latitude, double longitude, double elevationM) {
        // Geçersiz değerlerle nesne oluşturulması engellenir
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
     * İki nokta arasındaki kuş uçuşu mesafeyi (km) Haversine formülüyle hesaplar.
     * Dünya küre kabul edilir ve iki nokta arasındaki yay uzunluğu bulunur.
     */
    public double distanceKmTo(Location other) {
        // Trigonometrik fonksiyonlar radyan beklediği için açılar dönüştürülür
        double lat1 = Math.toRadians(latitude);
        double lat2 = Math.toRadians(other.latitude);
        double dLat = lat2 - lat1;
        double dLon = Math.toRadians(other.longitude - longitude);

        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(lat1) * Math.cos(lat2) * Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return EARTH_RADIUS_KM * c;
    }

    // Getter metotları: alanlara yalnızca okuma erişimi sağlar

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

    // Ad ve koordinatları aynı olan iki konum eşit kabul edilir
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Location other)) return false;
        return Double.compare(latitude, other.latitude) == 0
                && Double.compare(longitude, other.longitude) == 0
                && name.equals(other.name);
    }

    // equals ile aynı alanlar kullanılır
    @Override
    public int hashCode() {
        return Objects.hash(name, latitude, longitude);
    }

    @Override
    public String toString() {
        return name;
    }
}
