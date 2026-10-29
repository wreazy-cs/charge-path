package com.chargepath.routing;

import com.chargepath.TestData;
import com.chargepath.energy.PhysicsEnergyModel;
import com.chargepath.model.ChargingStation;
import com.chargepath.model.ElectricVehicle;
import com.chargepath.model.Route;
import com.chargepath.model.RouteLeg;
import com.chargepath.model.TripConditions;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RoutePlannerTest {

    private final TripConditions mild = new TripConditions(90, 20, false);
    private final ElectricVehicle car = TestData.sedan(); // 60 kWh, 150 Wh/km

    private RoutePlanner plannerWith(ChargingStation... stations) {
        return new RoutePlanner(TestData.stations(stations), new PhysicsEnergyModel());
    }

    @Test
    void enoughBatteryMeansDirectTripWithoutStops() {
        RoutePlanner planner = plannerWith(TestData.dcStation(TestData.LULEBURGAZ));
        TripRequest request = new TripRequest(car, TestData.KIRKLARELI, TestData.EDIRNE, 100, mild);

        Route route = planner.plan(request, new FastestStrategy()).orElseThrow();

        assertEquals(0, route.getStopCount());
        assertEquals(1, route.getLegs().size());
        assertEquals(0.0, route.getTotalCost(), 1e-9);
    }

    @Test
    void lowBatteryForcesAChargingStop() {
        RoutePlanner planner = plannerWith(TestData.dcStation(TestData.LULEBURGAZ));
        TripRequest request = new TripRequest(car, TestData.KIRKLARELI, TestData.ISTANBUL, 40, mild);

        Route route = planner.plan(request, new CheapestStrategy()).orElseThrow();

        assertEquals(1, route.getStopCount());
        assertEquals("DC-1", route.getLegs().get(0).getStation().getId());
    }

    @Test
    void noRouteWhenBatteryAndStationsAreNotEnough() {
        RoutePlanner planner = plannerWith(); // hiç istasyon yok
        TripRequest request = new TripRequest(car, TestData.KIRKLARELI, TestData.ISTANBUL, 30, mild);

        Optional<Route> route = planner.plan(request, new CheapestStrategy());

        assertTrue(route.isEmpty());
    }

    @Test
    void everyLegKeepsTheReserve() {
        RoutePlanner planner = plannerWith(TestData.dcStation(TestData.LULEBURGAZ), TestData.acStation(TestData.EDIRNE));
        TripRequest request = new TripRequest(car, TestData.KIRKLARELI, TestData.ISTANBUL, 35, mild);
        double reserveKwh = car.getBatteryKwh() * RoutePlanner.DEFAULT_RESERVE_PERCENT / 100;

        Route route = planner.plan(request, new FastestStrategy()).orElseThrow();

        for (RouteLeg leg : route.getLegs()) {
            assertTrue(leg.getArrivalSocKwh() >= reserveKwh - 1e-9, "Hiçbir bacakta yedek pay delinmemeli");
        }
    }

    @Test
    void cheapestIsNeverMoreExpensiveAndFastestIsNeverSlower() {
        // Aynı yerde biri ucuz/yavaş (AC), biri pahalı/hızlı (DC) iki istasyon
        ChargingStation ac = TestData.acStation(TestData.LULEBURGAZ);
        ChargingStation dc = TestData.dcStation(TestData.LULEBURGAZ);
        RoutePlanner planner = plannerWith(ac, dc);
        TripRequest request = new TripRequest(car, TestData.KIRKLARELI, TestData.ISTANBUL, 40, mild);

        Route cheapest = planner.plan(request, new CheapestStrategy()).orElseThrow();
        Route fastest = planner.plan(request, new FastestStrategy()).orElseThrow();

        assertTrue(cheapest.getTotalCost() <= fastest.getTotalCost());
        assertTrue(fastest.getTotalHours() <= cheapest.getTotalHours());
        assertEquals("AC", cheapest.getLegs().get(0).getStation().getTypeName());
        assertEquals("DC", fastest.getLegs().get(0).getStation().getTypeName());
    }
}
