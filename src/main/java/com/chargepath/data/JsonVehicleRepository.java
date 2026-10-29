package com.chargepath.data;

import com.chargepath.model.ElectricVehicle;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/** Araçları JSON dosyasından okuyan depo. Dosya bir kez okunur, sonra bellekte tutulur. */
public class JsonVehicleRepository implements VehicleRepository {

    private final List<ElectricVehicle> vehicles = new ArrayList<>();

    public JsonVehicleRepository(Path file) {
        for (VehicleRecord record : JsonFiles.readList(file, VehicleRecord[].class)) {
            vehicles.add(VehicleFactory.create(record));
        }
    }

    @Override
    public List<ElectricVehicle> findAll() {
        return Collections.unmodifiableList(vehicles);
    }

    @Override
    public Optional<ElectricVehicle> findById(String id) {
        for (ElectricVehicle v : vehicles) {
            if (v.getId().equals(id)) return Optional.of(v);
        }
        return Optional.empty();
    }
}
