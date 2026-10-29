package com.chargepath.model;

import com.chargepath.TestData;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ElectricVehicleTest {

    @Test
    void newVehicleStartsFull() {
        assertEquals(100.0, TestData.sedan().getSocPercent(), 1e-9);
    }

    @Test
    void consumeReducesBattery() {
        ElectricVehicle car = TestData.sedan();
        car.consume(15);
        assertEquals(45.0, car.getSocKwh(), 1e-9);
    }

    @Test
    void consumingMoreThanAvailableThrowsAndKeepsLevel() {
        ElectricVehicle car = TestData.sedan();
        car.setSocPercent(10); // 6 kWh
        assertThrows(IllegalStateException.class, () -> car.consume(7));
        assertEquals(6.0, car.getSocKwh(), 1e-9);
    }

    @Test
    void chargingNeverExceedsCapacity() {
        ElectricVehicle car = TestData.sedan();
        car.setSocPercent(90);
        car.charge(100);
        assertEquals(60.0, car.getSocKwh(), 1e-9);
    }

    @Test
    void invalidBatteryCapacityIsRejected() {
        assertThrows(IllegalArgumentException.class,
                () -> new Sedan("x", "X", "Y", 0, 150, 1800, 150, 11));
    }

    @Test
    void suvHasMoreDragThanSedan() {
        // Çok biçimlilik: aynı metot çağrısı, alt sınıfa göre farklı sonuç
        ElectricVehicle sedan = TestData.sedan();
        ElectricVehicle suv = TestData.suv();
        assertTrue(suv.getDragFactor() > sedan.getDragFactor());
        assertEquals("SUV", suv.getTypeName());
    }
}
