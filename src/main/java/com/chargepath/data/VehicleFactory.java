package com.chargepath.data;

import com.chargepath.model.ElectricVehicle;
import com.chargepath.model.Sedan;
import com.chargepath.model.Suv;

/**
 * Factory (fabrika) tasarım kalıbı.
 *
 * Hangi alt sınıftan nesne üretileceği kararı tek bir yerde verilir.
 * Programın geri kalanı aracın tipini bilmeden ElectricVehicle ile çalışır.
 * Yeni bir araç tipi (örneğin Hatchback) yalnızca bu sınıfta değişiklik gerektirir.
 *
 *   type = "SUV"   ->  new Suv(...)
 *   type = "SEDAN" ->  new Sedan(...)
 */
public final class VehicleFactory {

    // Yalnızca static metot içerir, nesnesi oluşturulmaz
    private VehicleFactory() {
    }

    public static ElectricVehicle create(VehicleRecord r) {
        if (r == null || r.type == null) {
            throw new IllegalArgumentException("Araç kaydı ya da tipi boş");
        }
        // trim ve toUpperCase ile büyük/küçük harf ve boşluk farkları yok sayılır
        return switch (r.type.trim().toUpperCase()) {
            case "SEDAN" -> new Sedan(r.id, r.brand, r.model, r.batteryKwh, r.consumptionWhPerKm,
                    r.massKg, r.maxDcKw, r.maxAcKw);
            case "SUV" -> new Suv(r.id, r.brand, r.model, r.batteryKwh, r.consumptionWhPerKm,
                    r.massKg, r.maxDcKw, r.maxAcKw);
            default -> throw new IllegalArgumentException("Bilinmeyen araç tipi: " + r.type);
        };
    }
}
