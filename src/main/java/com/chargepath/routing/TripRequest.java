package com.chargepath.routing;

import com.chargepath.model.ElectricVehicle;
import com.chargepath.model.Location;
import com.chargepath.model.TripConditions;

/** Kullanıcının rota isteği: hangi araçla, nereden, nereye, kaç yüzde bataryayla, hangi koşullarda. */
public final class TripRequest {

    private final ElectricVehicle vehicle;
    private final Location from;
    private final Location to;
    private final double startSocPercent;
    private final TripConditions conditions;

    public TripRequest(ElectricVehicle vehicle, Location from, Location to,
                       double startSocPercent, TripConditions conditions) {
        if (vehicle == null || from == null || to == null || conditions == null) {
            throw new IllegalArgumentException("Araç, başlangıç, varış ve koşullar boş olamaz");
        }
        if (from.equals(to)) {
            throw new IllegalArgumentException("Başlangıç ve varış aynı olamaz");
        }
        if (startSocPercent <= 0 || startSocPercent > 100) {
            throw new IllegalArgumentException("Başlangıç bataryası 0'dan büyük, en fazla 100 olmalı");
        }
        this.vehicle = vehicle;
        this.from = from;
        this.to = to;
        this.startSocPercent = startSocPercent;
        this.conditions = conditions;
    }

    public ElectricVehicle getVehicle() {
        return vehicle;
    }

    public Location getFrom() {
        return from;
    }

    public Location getTo() {
        return to;
    }

    public double getStartSocPercent() {
        return startSocPercent;
    }

    public double getStartSocKwh() {
        return vehicle.getBatteryKwh() * startSocPercent / 100.0;
    }

    public TripConditions getConditions() {
        return conditions;
    }
}
