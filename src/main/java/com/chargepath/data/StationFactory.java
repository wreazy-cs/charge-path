package com.chargepath.data;

import com.chargepath.model.AcStation;
import com.chargepath.model.ChargingStation;
import com.chargepath.model.DcFastStation;
import com.chargepath.model.Location;

/**
 * Factory kalıbı: JSON kaydındaki tipe göre uygun istasyon sınıfını üretir.
 *   type = "AC" -> AcStation,  type = "DC" -> DcFastStation
 */
public final class StationFactory {

    private StationFactory() {
    }

    public static ChargingStation create(StationRecord r) {
        if (r == null || r.type == null) {
            throw new IllegalArgumentException("İstasyon kaydı ya da tipi boş");
        }
        Location location = new Location(r.name, r.lat, r.lon, r.elevationM);
        return switch (r.type.trim().toUpperCase()) {
            case "AC" -> new AcStation(r.id, r.name, r.operator, r.city, location, r.powerKw, r.pricePerKwh);
            case "DC" -> new DcFastStation(r.id, r.name, r.operator, r.city, location, r.powerKw, r.pricePerKwh);
            default -> throw new IllegalArgumentException("Bilinmeyen istasyon tipi: " + r.type);
        };
    }
}
