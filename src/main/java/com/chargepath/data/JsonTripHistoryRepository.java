package com.chargepath.data;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Yolculuk geçmişini trip_history.json dosyasında tutan depo.
 * Her kayıtta dosya okunur, yeni kayıt eklenir ve dosya yeniden yazılır.
 * Veri miktarı küçük olduğu için bu yöntem yeterlidir.
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
        // İlk çalıştırmada dosya bulunmaz; bu durumda boş liste döner
        if (!Files.exists(file)) {
            return new ArrayList<>();
        }
        return JsonFiles.readList(file, TripRecord[].class);
    }
}
