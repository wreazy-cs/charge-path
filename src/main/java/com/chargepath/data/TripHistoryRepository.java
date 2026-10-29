package com.chargepath.data;

import java.util.List;

// Yolculuk geçmişini kaydetme ve okuma arayüzü
public interface TripHistoryRepository {

    void save(TripRecord record);

    List<TripRecord> findAll();
}
