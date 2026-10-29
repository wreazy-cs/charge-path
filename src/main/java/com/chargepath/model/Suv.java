package com.chargepath.model;

/**
 * SUV tipi araç: yüksek ve geniş gövdesi nedeniyle yüksek hızda
 * sedana göre daha fazla hava direnciyle karşılaşır.
 */
public class Suv extends ElectricVehicle {

    // Varsayım: hava direnci sedana göre yaklaşık %15 fazladır
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
