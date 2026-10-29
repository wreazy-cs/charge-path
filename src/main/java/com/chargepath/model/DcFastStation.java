package com.chargepath.model;

/**
 * DC (doğru akım) hızlı şarj istasyonu: hızlı, genellikle daha pahalıdır.
 * Lityum iyon batarya doldukça şarj gücü düşer. Basitleştirme olarak %10 ile %80
 * arasındaki ortalama gücün, en yüksek gücün %85'i olduğu varsayılmıştır.
 */
public class DcFastStation extends ChargingStation {

    private static final double AVERAGE_CURVE_FACTOR = 0.85;

    public DcFastStation(String id, String name, String operator, String city,
                         Location location, double powerKw, double pricePerKwh) {
        super(id, name, operator, city, location, powerKw, pricePerKwh);
    }

    // İstasyon ve araç sınırlarından küçük olanı alınır, ortalama katsayıyla çarpılır
    @Override
    public double effectivePowerKw(ElectricVehicle vehicle) {
        return Math.min(getPowerKw(), vehicle.getMaxDcKw()) * AVERAGE_CURVE_FACTOR;
    }

    @Override
    public String getTypeName() {
        return "DC";
    }
}
