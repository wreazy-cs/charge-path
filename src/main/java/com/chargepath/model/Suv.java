package com.chargepath.model;

/**
 * SUV: yüksek ve geniş gövde, yüksek hızda hava direnci sedana göre daha fazla.
 */
public class Suv extends ElectricVehicle {

    // Varsayım: SUV'un ön yüzey alanı ve sürtünme katsayısı sedana göre yaklaşık %15 fazla.
    private static final double DRAG_FACTOR = 1.15;

    public Suv(String id, String brand, String model, double batteryKwh,
               double consumptionWhPerKm, double massKg, double maxDcKw, double maxAcKw) {
        super(id, brand, model, batteryKwh, consumptionWhPerKm, massKg, maxDcKw, maxAcKw);
    }

    @Override
    public double getDragFactor() {
        return DRAG_FACTOR;
    }

    @Override
    public String getTypeName() {
        return "SUV";
    }
}
