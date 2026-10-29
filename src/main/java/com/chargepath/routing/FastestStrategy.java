package com.chargepath.routing;

// En hızlı rota: sürüş süresi, şarj süresi ve durak kayıplarının toplamını en aza indirir
public class FastestStrategy implements RouteStrategy {

    // Varsayım: her şarj durağında park, bağlantı ve ödeme için 5 dakika kaybedilir
    private static final double STOP_OVERHEAD_HOURS = 5.0 / 60.0;

    @Override
    public String getName() {
        return "En hızlı";
    }

    // Sürüşün ağırlığı sürüş süresidir (saat)
    @Override
    public double driveWeight(double roadKm, double driveHours) {
        return driveHours;
    }

    // Şarjın ağırlığı şarj süresi ile durak kaybının toplamıdır (saat)
    @Override
    public double chargeWeight(double chargedKwh, double cost, double chargeHours) {
        return chargeHours + STOP_OVERHEAD_HOURS;
    }
}
