package com.chargepath.data;

import com.chargepath.model.ElectricVehicle;
import com.chargepath.model.Sedan;
import com.chargepath.model.Suv;

/**
 * Fabrika (Factory) tasarım kalıbı.
 *
 * "Hangi alt sınıftan nesne üretilecek?" kararı tek bir yerde verilir.
 * Programın geri kalanı Sedan mı Suv mu olduğunu bilmeden ElectricVehicle ile çalışır.
 * Yeni bir araç tipi eklenirse (örn. Hatchback) sadece bu sınıf değişir.
 *
 *   VehicleRecord(type="SUV")   --create()-->  new Suv(...)
 *   VehicleRecord(type="SEDAN") --create()-->  new Sedan(...)
 */
public final class VehicleFactory {

    private VehicleFactory() {
        // Sadece static metot içerir, nesnesi üretilmez.
    }

    public static ElectricVehicle create(VehicleRecord r) {
        if (r == null || r.type == null) {
            throw new IllegalArgumentException("Araç kaydı ya da tipi boş");
        }
        return switch (r.type.trim().toUpperCase()) {
            case "SEDAN" -> new Sedan(r.id, r.brand, r.model, r.batteryKwh, r.consumptionWhPerKm,
                    r.massKg, r.maxDcKw, r.maxAcKw);
            case "SUV" -> new Suv(r.id, r.brand, r.model, r.batteryKwh, r.consumptionWhPerKm,
                    r.massKg, r.maxDcKw, r.maxAcKw);
            default -> throw new IllegalArgumentException("Bilinmeyen araç tipi: " + r.type);
        };
    }
}
