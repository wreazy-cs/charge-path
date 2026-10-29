package com.chargepath;

import com.chargepath.data.StationRepository;
import com.chargepath.model.AcStation;
import com.chargepath.model.ChargingStation;
import com.chargepath.model.DcFastStation;
import com.chargepath.model.ElectricVehicle;
import com.chargepath.model.Location;
import com.chargepath.model.Sedan;
import com.chargepath.model.Suv;

import java.util.List;
import java.util.Optional;

/** Testlerde tekrar kullanılan hazır nesneler. Gerçek veriye benzer ama sabit ve küçük. */
public final class TestData {

    public static final Location KIRKLARELI = new Location("Kırklareli", 41.7351, 27.2252, 216);
    public static final Location EDIRNE = new Location("Edirne", 41.6771, 26.5557, 63);
    public static final Location LULEBURGAZ = new Location("Lüleburgaz", 41.4039, 27.3569, 65);
    public static final Location ISTANBUL = new Location("İstanbul (Fatih)", 41.0082, 28.9784, 39);

    private TestData() {
    }

    public static ElectricVehicle sedan() {
        return new Sedan("test-sedan", "Test", "Sedan", 60, 150, 1800, 150, 11);
    }

    public static ElectricVehicle suv() {
        return new Suv("test-suv", "Test", "SUV", 60, 150, 1800, 150, 11);
    }

    public static AcStation acStation(Location at) {
        return new AcStation("AC-1", "Test AC", "Test", at.getName(), at, 22, 10.0);
    }

    public static DcFastStation dcStation(Location at) {
        return new DcFastStation("DC-1", "Test DC", "Test", at.getName(), at, 180, 15.0);
    }

    /** Bellekte çalışan sahte istasyon deposu: JSON dosyası gerekmez (arayüz sayesinde mümkün). */
    public static StationRepository stations(ChargingStation... list) {
        List<ChargingStation> stations = List.of(list);
        return new StationRepository() {
            @Override
            public List<ChargingStation> findAll() {
                return stations;
            }

            @Override
            public Optional<ChargingStation> findById(String id) {
                return stations.stream().filter(s -> s.getId().equals(id)).findFirst();
            }
        };
    }
}
