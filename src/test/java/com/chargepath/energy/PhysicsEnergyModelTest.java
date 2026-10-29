package com.chargepath.energy;

import com.chargepath.TestData;
import com.chargepath.model.ElectricVehicle;
import com.chargepath.model.Location;
import com.chargepath.model.TripConditions;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PhysicsEnergyModelTest {

    private final PhysicsEnergyModel model = new PhysicsEnergyModel();
    private final ElectricVehicle car = TestData.sedan();
    private final Location low = new Location("Alçak", 41.0, 27.0, 0);
    private final Location high = new Location("Yüksek", 41.0, 27.1, 500);
    private final TripConditions mild = new TripConditions(90, 20, false);

    @Test
    void flatRoadAtReferenceSpeedMatchesCatalogConsumption() {
        // 90 km/s, 20 °C, klima kapalı, düz yol: 100 km x 150 Wh/km = 15 kWh
        Location a = new Location("A", 41.0, 27.0, 100);
        Location b = new Location("B", 41.0, 27.5, 100);
        assertEquals(15.0, model.legEnergyKwh(car, a, b, 100, mild), 1e-6);
    }

    @Test
    void higherSpeedNeedsMoreEnergy() {
        double slow = model.legEnergyKwh(car, low, low, 100, new TripConditions(80, 20, false));
        double fast = model.legEnergyKwh(car, low, low, 100, new TripConditions(130, 20, false));
        assertTrue(fast > slow * 1.3, "130 km/s, 80 km/s'den belirgin şekilde fazla harcamalı");
    }

    @Test
    void uphillCostsMoreThanDownhillThanksToRegenerativeBraking() {
        double up = model.legEnergyKwh(car, low, high, 10, mild);
        double down = model.legEnergyKwh(car, high, low, 10, mild);
        double flat = model.legEnergyKwh(car, low, low, 10, mild);
        assertTrue(up > flat, "Tırmanış düz yoldan fazla harcamalı");
        assertTrue(down < flat, "İnişte rejeneratif fren enerji geri kazandırmalı");
    }

    @Test
    void winterWithHeatingShortensRange() {
        double summerRange = model.rangeKm(car, 40, mild);
        double winterRange = model.rangeKm(car, 40, new TripConditions(90, -5, true));
        assertTrue(winterRange < summerRange * 0.85, "Kışın ısıtmayla menzil en az %15 düşmeli");
    }

    @Test
    void climateOnUsesMoreEnergyThanOff() {
        double off = model.legEnergyKwh(car, low, low, 50, new TripConditions(90, 35, false));
        double on = model.legEnergyKwh(car, low, low, 50, new TripConditions(90, 35, true));
        assertTrue(on > off);
    }
}
