package com.chargepath.energy;

import com.chargepath.model.ElectricVehicle;
import com.chargepath.model.Location;
import com.chargepath.model.TripConditions;

/**
 * Enerji hesabı için arayüz.
 *
 * RoutePlanner somut sınıfa değil bu arayüze bağlıdır. Farklı bir enerji modeli
 * planlayıcıda değişiklik yapmadan kullanılabilir (Bağımlılığın Tersine Çevrilmesi, DIP).
 */
public interface EnergyModel {

    // İki nokta arasındaki yolda harcanacak enerji (kWh)
    double legEnergyKwh(ElectricVehicle vehicle, Location from, Location to, double roadKm, TripConditions conditions);

    // Düz yolda, verilen enerjiyle gidilebilecek mesafe (km)
    double rangeKm(ElectricVehicle vehicle, double availableKwh, TripConditions conditions);
}
