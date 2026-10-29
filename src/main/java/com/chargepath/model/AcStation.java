package com.chargepath.model;

/**
 * AC (alternatif akım) istasyonu: yavaş ama genelde ucuz.
 * Akımı aracın içindeki şarj cihazı (on-board charger) dönüştürür,
 * bu yüzden hız, istasyon ile aracın AC sınırının küçüğüdür.
 */
public class AcStation extends ChargingStation {

    public AcStation(String id, String name, String operator, String city,
                     Location location, double powerKw, double pricePerKwh) {
        super(id, name, operator, city, location, powerKw, pricePerKwh);
    }

    @Override
    public double effectivePowerKw(ElectricVehicle vehicle) {
        return Math.min(getPowerKw(), vehicle.getMaxAcKw());
    }

    @Override
    public String getTypeName() {
        return "AC";
    }
}
