package com.chargepath.model;

/**
 * Yolculuk koşulları: hız, dış sıcaklık ve klima.
 * Enerji modeli bu bilgilere bakarak tüketimi artırır ya da azaltır.
 */
public final class TripConditions {

    private final double speedKmh;
    private final double temperatureC;
    private final boolean climateOn;

    public TripConditions(double speedKmh, double temperatureC, boolean climateOn) {
        if (speedKmh < 20 || speedKmh > 150) {
            throw new IllegalArgumentException("Hız 20 ile 150 km/s arasında olmalı: " + speedKmh);
        }
        if (temperatureC < -30 || temperatureC > 50) {
            throw new IllegalArgumentException("Sıcaklık -30 ile 50 °C arasında olmalı: " + temperatureC);
        }
        this.speedKmh = speedKmh;
        this.temperatureC = temperatureC;
        this.climateOn = climateOn;
    }

    /** Aşırı yükleme (overloading) örneği: klima belirtilmezse kapalı kabul edilir. */
    public TripConditions(double speedKmh, double temperatureC) {
        this(speedKmh, temperatureC, false);
    }

    public double getSpeedKmh() {
        return speedKmh;
    }

    public double getTemperatureC() {
        return temperatureC;
    }

    public boolean isClimateOn() {
        return climateOn;
    }

    @Override
    public String toString() {
        return String.format("%.0f km/s, %.0f °C, klima %s", speedKmh, temperatureC, climateOn ? "açık" : "kapalı");
    }
}
