package com.chargepath.data;

import com.chargepath.model.ChargingStation;

import java.util.List;
import java.util.Optional;

/**
 * İstasyon verilerine erişim arayüzü (Repository kalıbı).
 * Planlayıcının ihtiyacı olan iki metotla sınırlıdır (Arayüz Ayrımı ilkesi, ISP).
 */
public interface StationRepository {

    List<ChargingStation> findAll();

    // Kayıt bulunamazsa boş Optional döner
    Optional<ChargingStation> findById(String id);
}
