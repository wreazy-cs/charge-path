package com.chargepath.data;

import com.chargepath.model.ChargingStation;
import com.chargepath.model.DcFastStation;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JsonRepositoryTest {

    @Test
    void fallbackStationFileLoadsAllStations() {
        StationRepository repo = new JsonStationRepository(Path.of("data/stations_fallback.json"));
        List<ChargingStation> all = repo.findAll();
        assertEquals(28, all.size());
        long dc = all.stream().filter(s -> s.getTypeName().equals("DC")).count();
        assertEquals(17, dc);
        assertInstanceOf(DcFastStation.class, repo.findById("EDR-01").orElseThrow());
    }

    @Test
    void fallbackVehicleFileLoadsFourVehicles() {
        VehicleRepository repo = new JsonVehicleRepository(Path.of("data/vehicles_fallback.json"));
        assertEquals(4, repo.findAll().size());
        assertTrue(repo.findById("togg-t10x-lr").isPresent());
    }

    @Test
    void missingFileGivesClearError() {
        assertThrows(DataAccessException.class, () -> new JsonStationRepository(Path.of("yok/olmayan.json")));
    }

    @Test
    void tripHistoryIsPersistedBetweenRepositoryInstances(@TempDir Path tempDir) {
        Path file = tempDir.resolve("trip_history.json");
        TripRecord record = new TripRecord("29.10.2026 10:00", "Togg T10X", "Kırklareli", "Edirne",
                "En ucuz", 72.5, 15.1, 0, 0.8, 0, List.of());

        new JsonTripHistoryRepository(file).save(record);
        // Yeni bir depo nesnesi = programın kapanıp yeniden açılması gibi
        List<TripRecord> loaded = new JsonTripHistoryRepository(file).findAll();

        assertEquals(1, loaded.size());
        assertEquals("Kırklareli", loaded.get(0).getFrom());
        assertEquals(72.5, loaded.get(0).getDistanceKm(), 1e-9);
    }
}
