package com.chargepath.model;

/**
 * Sedan tipi araç: alçak gövdeli ve aerodinamiktir.
 * ElectricVehicle sınıfından kalıtım alır; yalnızca hava direnci ve tip adını tanımlar.
 */
public class Sedan extends ElectricVehicle {

    // Referans araç tipi olduğu için katsayı 1'dir
    private static final double DRAG_FACTOR = 1.0;

    public Sedan(String id, String brand, String model, double batteryKwh,
                 double consumptionWhPerKm, double massKg, double maxDcKw, double maxAcKw) {
        // Değerler doğrulama için üst sınıfın kurucusuna iletilir
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
