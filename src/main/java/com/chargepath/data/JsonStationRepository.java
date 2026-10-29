package com.chargepath.data;

import com.chargepath.model.ChargingStation;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

// İstasyonları stations_fallback.json dosyasından okuyan depo (çevrimdışı çalışır)
public class JsonStationRepository implements StationRepository {

    private final List<ChargingStation> stations = new ArrayList<>();

    // Her kayıt StationFactory ile AC ya da DC istasyon nesnesine dönüştürülür
    public JsonStationRepository(Path file) {
        for (StationRecord record : JsonFiles.readList(file, StationRecord[].class)) {
            stations.add(StationFactory.create(record));
        }
    }

    @Override
    public List<ChargingStation> findAll() {
        return Collections.unmodifiableList(stations);
    }

    // Kimliği eşleşen istasyon liste üzerinde aranır
    @Override
    public Optional<ChargingStation> findById(String id) {
        for (ChargingStation s : stations) {
            if (s.getId().equals(id)) return Optional.of(s);
        }
        return Optional.empty();
    }
}
