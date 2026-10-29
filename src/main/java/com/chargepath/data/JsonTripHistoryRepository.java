package com.chargepath.data;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Yolculuk geçmişini JSON dosyasında tutan depo.
 * Her kayıtta dosyanın tamamı okunur, yeni kayıt eklenir ve dosya yeniden yazılır.
 * Küçük veri için en basit ve güvenilir yöntem.
 */
public class JsonTripHistoryRepository implements TripHistoryRepository {

    private final Path file;

    public JsonTripHistoryRepository(Path file) {
        this.file = file;
    }

    @Override
    public void save(TripRecord record) {
        List<TripRecord> all = findAll();
        all.add(record);
        JsonFiles.writeList(file, all);
    }

    @Override
    public List<TripRecord> findAll() {
        if (!Files.exists(file)) {
            return new ArrayList<>(); // henüz hiç yolculuk kaydedilmemiş
        }
        return JsonFiles.readList(file, TripRecord[].class);
    }
}
