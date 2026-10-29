package com.chargepath.model;

/**
 * DC (doğru akım) hızlı şarj istasyonu: hızlı ama pahalı.
 * Batarya doldukça şarj gücü düşer (şarj eğrisi). Basitleştirme olarak
 * %10-%80 aralığındaki ortalama gücün, tepe gücün %85'i olduğunu varsayıyoruz.
 */
public class DcFastStation extends ChargingStation {

    private static final double AVERAGE_CURVE_FACTOR = 0.85;

    public DcFastStation(String id, String name, String operator, String city,
                         Location location, double powerKw, double pricePerKwh) {
        super(id, name, operator, city, location, powerKw, pricePerKwh);
    }

    @Override
    public double effectivePowerKw(ElectricVehicle vehicle) {
        return Math.min(getPowerKw(), vehicle.getMaxDcKw()) * AVERAGE_CURVE_FACTOR;
    }

    @Override
    public String getTypeName() {
        return "DC";
    }
}
