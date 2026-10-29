package com.chargepath.routing;

// En ucuz rota: yol boyunca ödenen toplam şarj ücretini en aza indirir
public class CheapestStrategy implements RouteStrategy {

    // Ücretler eşitse kısa rotanın seçilmesi için km başına küçük bir pay
    private static final double TIE_BREAK_PER_KM = 0.001;

    @Override
    public String getName() {
        return "En ucuz";
    }

    // Sürüşün parasal maliyeti yoktur, yalnızca eşitlik payı eklenir
    @Override
    public double driveWeight(double roadKm, double driveHours) {
        return roadKm * TIE_BREAK_PER_KM;
    }

    // Şarjın ağırlığı ödenen ücrettir (TL)
    @Override
    public double chargeWeight(double chargedKwh, double cost, double chargeHours) {
        return cost;
    }
}
