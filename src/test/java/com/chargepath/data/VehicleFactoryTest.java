package com.chargepath.data;

import com.chargepath.model.ElectricVehicle;
import com.chargepath.model.Sedan;
import com.chargepath.model.Suv;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;

class VehicleFactoryTest {

    private VehicleRecord record(String type) {
        VehicleRecord r = new VehicleRecord();
        r.id = "x";
        r.brand = "Togg";
        r.model = "T10X";
        r.type = type;
        r.batteryKwh = 85;
        r.consumptionWhPerKm = 195;
        r.massKg = 2248;
        r.maxDcKw = 180;
        r.maxAcKw = 11;
        return r;
    }

    @Test
    void suvTypeCreatesSuv() {
        ElectricVehicle v = VehicleFactory.create(record("SUV"));
        assertInstanceOf(Suv.class, v);
        assertEquals(85.0, v.getBatteryKwh(), 1e-9);
    }

    @Test
    void typeIsCaseInsensitive() {
        assertInstanceOf(Sedan.class, VehicleFactory.create(record("sedan")));
    }

    @Test
    void unknownTypeIsRejected() {
        assertThrows(IllegalArgumentException.class, () -> VehicleFactory.create(record("KAMYON")));
    }
}
