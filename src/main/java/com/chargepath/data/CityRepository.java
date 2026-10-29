package com.chargepath.data;

import com.chargepath.model.Location;

import java.util.List;

// Başlangıç ve varış olarak seçilebilecek şehirlere erişim arayüzü
public interface CityRepository {

    List<Location> findAll();
}
