package com.chargepath.routing;

/** En ucuz rota: yol boyunca ödenen şarj ücretini en aza indirir. */
public class CheapestStrategy implements RouteStrategy {

    // Ücret eşitse kısa yolu seçmek için çok küçük bir mesafe payı (eşitlik bozucu), km başına 0.001 TL.
    private static final double TIE_BREAK_PER_KM = 0.001;

    @Override
    public String getName() {
        return "En ucuz";
    }

    @Override
    public double driveWeight(double roadKm, double driveHours) {
        return roadKm * TIE_BREAK_PER_KM;
    }

    @Override
    public double chargeWeight(double chargedKwh, double cost, double chargeHours) {
        return cost;
    }
}
