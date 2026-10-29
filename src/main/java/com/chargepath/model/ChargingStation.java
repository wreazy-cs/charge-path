package com.chargepath.model;

import java.util.Objects;

/**
 * Tüm şarj istasyonlarının ortak şablonu (soyut sınıf).
 *
 * Her istasyon tipi "bu araca gerçekte kaç kW verebilirim?" sorusunu
 * kendine göre cevaplar (effectivePowerKw). Rota planlayıcı istasyonun
 * AC mi DC mi olduğunu sormaz (instanceof yok), sadece bu metodu çağırır.
 * Bu, Liskov yerine geçme ilkesinin (LSP) uygulamasıdır.
 */
public abstract class ChargingStation {

    private final String id;
    private final String name;
    private final String operator;
    private final String city;
    private final Location location;
    private final double powerKw;      // istasyonun etiket gücü
    private final double pricePerKwh;  // TL / kWh

    protected ChargingStation(String id, String name, String operator, String city,
                              Location location, double powerKw, double pricePerKwh) {
        if (id == null || id.isBlank()) throw new IllegalArgumentException("İstasyon kimliği boş olamaz");
        if (location == null) throw new IllegalArgumentException("İstasyon konumu boş olamaz");
        if (powerKw <= 0) throw new IllegalArgumentException("İstasyon gücü 0'dan büyük olmalı");
        if (pricePerKwh < 0) throw new IllegalArgumentException("Fiyat negatif olamaz");
        this.id = id;
        this.name = name;
        this.operator = operator;
        this.city = city;
        this.location = location;
        this.powerKw = powerKw;
        this.pricePerKwh = pricePerKwh;
    }

    /** Bu istasyonun verilen araca ortalama olarak verebileceği güç (kW). */
    public abstract double effectivePowerKw(ElectricVehicle vehicle);

    /** "AC" ya da "DC". */
    public abstract String getTypeName();

    /** Belirtilen enerjiyi bu istasyonda bu araca yüklemek kaç saat sürer? */
    public double chargingTimeHours(double kwh, ElectricVehicle vehicle) {
        if (kwh <= 0) return 0;
        return kwh / effectivePowerKw(vehicle);
    }

    /** Belirtilen enerjinin ücreti (TL). */
    public double chargingCost(double kwh) {
        return Math.max(0, kwh) * pricePerKwh;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getOperator() {
        return operator;
    }

    public String getCity() {
        return city;
    }

    public Location getLocation() {
        return location;
    }

    public double getPowerKw() {
        return powerKw;
    }

    public double getPricePerKwh() {
        return pricePerKwh;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ChargingStation other)) return false;
        return id.equals(other.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return String.format("%s %s (%s, %.0f kW %s, %.2f TL/kWh)", id, name, city, powerKw, getTypeName(), pricePerKwh);
    }
}
