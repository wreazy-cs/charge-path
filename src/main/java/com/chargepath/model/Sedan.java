package com.chargepath.model;

/**
 * Sedan: alçak gövdeli, aerodinamik araç.
 * extends ElectricVehicle: tüm alanları ve metotları üst sınıftan devralır,
 * sadece kendine özgü olanları (hava direnci, tip adı) tanımlar.
 */
public class Sedan extends ElectricVehicle {

    private static final double DRAG_FACTOR = 1.0;

    public Sedan(String id, String brand, String model, double batteryKwh,
                 double consumptionWhPerKm, double massKg, double maxDcKw, double maxAcKw) {
        super(id, brand, model, batteryKwh, consumptionWhPerKm, massKg, maxDcKw, maxAcKw);
    }

    @Override
    public double getDragFactor() {
        return DRAG_FACTOR;
    }

    @Override
    public String getTypeName() {
        return "Sedan";
    }
}
