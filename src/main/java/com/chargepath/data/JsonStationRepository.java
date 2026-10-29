package com.chargepath.data;

import com.chargepath.model.ChargingStation;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/** İstasyonları JSON dosyasından okuyan depo (çevrimdışı yedek veri). */
public class JsonStationRepository implements StationRepository {

    private final List<ChargingStation> stations = new ArrayList<>();

    public JsonStationRepository(Path file) {
        for (StationRecord record : JsonFiles.readList(file, StationRecord[].class)) {
            stations.add(StationFactory.create(record));
        }
    }

    @Override
    public List<ChargingStation> findAll() {
        return Collections.unmodifiableList(stations);
    }

    @Override
    public Optional<ChargingStation> findById(String id) {
        for (ChargingStation s : stations) {
            if (s.getId().equals(id)) return Optional.of(s);
        }
        return Optional.empty();
    }
}
