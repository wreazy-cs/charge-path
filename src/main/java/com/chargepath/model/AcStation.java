package com.chargepath.model;

/**
 * AC (alternatif akım) şarj istasyonu: yavaş, genellikle daha ucuzdur.
 * Akım, aracın dahili şarj cihazı tarafından dönüştürülür. Bu nedenle şarj gücü,
 * istasyon gücü ile aracın AC sınırından küçük olanıdır.
 */
public class AcStation extends ChargingStation {

    public AcStation(String id, String name, String operator, String city,
                     Location location, double powerKw, double pricePerKwh) {
        super(id, name, operator, city, location, powerKw, pricePerKwh);
    }

    // Örnek: istasyon 22 kW, araç 11 kW kabul ediyorsa şarj gücü 11 kW olur
    @Override
    public double effectivePowerKw(ElectricVehicle vehicle) {
        return Math.min(getPowerKw(), vehicle.getMaxAcKw());
    }

    @Override
    public String getTypeName() {
        return "AC";
    }
}
