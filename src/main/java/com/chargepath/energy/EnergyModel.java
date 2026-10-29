package com.chargepath.energy;

import com.chargepath.model.ElectricVehicle;
import com.chargepath.model.Location;
import com.chargepath.model.TripConditions;

/**
 * Enerji hesabının sözleşmesi (arayüz).
 *
 * RoutePlanner bu arayüze bağlıdır, somut sınıfa değil (Bağımlılığın Tersine
 * Çevrilmesi, DIP). Yarın daha basit ya da daha gerçekçi bir model yazılırsa
 * planlayıcıya dokunmadan takılır.
 */
public interface EnergyModel {

    /** İki nokta arasındaki yol için harcanacak enerji (kWh). */
    double legEnergyKwh(ElectricVehicle vehicle, Location from, Location to, double roadKm, TripConditions conditions);

    /** Düz yolda, verilen enerjiyle kaç km gidilebilir? */
    double rangeKm(ElectricVehicle vehicle, double availableKwh, TripConditions conditions);
}
