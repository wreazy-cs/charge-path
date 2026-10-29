package com.chargepath.data;

import com.chargepath.model.ElectricVehicle;

import java.util.List;
import java.util.Optional;

/**
 * Araç verisine erişim sözleşmesi (Repository kalıbı).
 * Verinin JSON'dan mı, veritabanından mı geldiğini kullanan taraf bilmez.
 */
public interface VehicleRepository {

    List<ElectricVehicle> findAll();

    Optional<ElectricVehicle> findById(String id);
}
