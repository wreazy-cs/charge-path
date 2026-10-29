package com.chargepath.routing;

/** En hızlı rota: sürüş süresi + şarj süresi + her durağın sabit kaybı en aza indirilir. */
public class FastestStrategy implements RouteStrategy {

    // Varsayım: her şarj durağında park etme, kablo takma, ödeme için 5 dakika.
    private static final double STOP_OVERHEAD_HOURS = 5.0 / 60.0;

    @Override
    public String getName() {
        return "En hızlı";
    }

    @Override
    public double driveWeight(double roadKm, double driveHours) {
        return driveHours;
    }

    @Override
    public double chargeWeight(double chargedKwh, double cost, double chargeHours) {
        return chargeHours + STOP_OVERHEAD_HOURS;
    }
}
