package com.chargepath.data;

import com.chargepath.model.Location;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Şehirleri JSON dosyasından okuyan depo. */
public class JsonCityRepository implements CityRepository {

    private final List<Location> cities = new ArrayList<>();

    public JsonCityRepository(Path file) {
        for (CityRecord r : JsonFiles.readList(file, CityRecord[].class)) {
            cities.add(new Location(r.name, r.lat, r.lon, r.elevationM));
        }
    }

    @Override
    public List<Location> findAll() {
        return Collections.unmodifiableList(cities);
    }
}
