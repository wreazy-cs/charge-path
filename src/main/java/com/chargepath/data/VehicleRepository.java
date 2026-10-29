package com.chargepath.data;

import com.chargepath.model.ElectricVehicle;

import java.util.List;
import java.util.Optional;

/**
 * Araç verilerine erişim arayüzü (Repository kalıbı).
 * Kullanan sınıf, verinin hangi kaynaktan geldiğini bilmez.
 */
public interface VehicleRepository {

    List<ElectricVehicle> findAll();

    Optional<ElectricVehicle> findById(String id);
}
