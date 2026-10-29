package com.chargepath.data;

import java.util.List;

/** Yolculuk geçmişini saklama sözleşmesi. */
public interface TripHistoryRepository {

    void save(TripRecord record);

    List<TripRecord> findAll();
}
