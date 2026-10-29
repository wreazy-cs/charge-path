package com.chargepath.model;

import com.chargepath.TestData;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

// Şarj istasyonu testleri: AC/DC güç, süre ve ücret hesabı
class ChargingStationTest {

    // AC şarj gücü aracın AC sınırını aşmamalıdır
    @Test
    void acPowerIsLimitedByVehicleOnBoardCharger() {
        // İstasyon gücü 22 kW, aracın AC sınırı 11 kW
        ChargingStation ac = TestData.acStation(TestData.KIRKLARELI);
        assertEquals(11.0, ac.effectivePowerKw(TestData.sedan()), 1e-9);
    }

    // Aynı enerji için DC şarj, AC'den en az 5 kat hızlı olmalıdır
    @Test
    void dcIsMuchFasterThanAcForTheSameEnergy() {
        ElectricVehicle car = TestData.sedan();
        ChargingStation ac = TestData.acStation(TestData.KIRKLARELI);
        ChargingStation dc = TestData.dcStation(TestData.KIRKLARELI);
        assertTrue(dc.chargingTimeHours(30, car) * 5 < ac.chargingTimeHours(30, car));
    }

    // Ücret = enerji x fiyat: 20 kWh x 15 TL = 300 TL
    @Test
    void costIsEnergyTimesPrice() {
        ChargingStation dc = TestData.dcStation(TestData.KIRKLARELI); // 15 TL/kWh
        assertEquals(300.0, dc.chargingCost(20), 1e-9);
    }
}
