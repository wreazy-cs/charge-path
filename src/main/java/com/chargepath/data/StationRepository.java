package com.chargepath.data;

import com.chargepath.model.ChargingStation;

import java.util.List;
import java.util.Optional;

/**
 * İstasyon verisine erişim sözleşmesi (Repository kalıbı).
 * Arayüz küçük tutuldu (Arayüz Ayrımı İlkesi, ISP): planlayıcının ihtiyacı sadece bunlar.
 */
public interface StationRepository {

    List<ChargingStation> findAll();

    Optional<ChargingStation> findById(String id);
}
