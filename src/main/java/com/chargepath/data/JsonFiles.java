package com.chargepath.data;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * JSON okuma ve yazma işini tek yerde toplayan yardımcı sınıf.
 * Tüm depolar (repository) bunu kullanır, böylece Gson ayrıntısı tek yerde kalır.
 */
public final class JsonFiles {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    private JsonFiles() {
        // Yardımcı sınıf: sadece static metotlar var, nesne üretilmesin diye kurucu private.
    }

    /** Dosyadaki JSON dizisini okuyup listeye çevirir. Dosya yoksa ya da bozuksa açıklayıcı hata verir. */
    public static <T> List<T> readList(Path file, Class<T[]> arrayType) {
        if (!Files.exists(file)) {
            throw new DataAccessException("Veri dosyası bulunamadı: " + file.toAbsolutePath());
        }
        try (Reader reader = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
            T[] items = GSON.fromJson(reader, arrayType);
            return items == null ? new ArrayList<>() : new ArrayList<>(Arrays.asList(items));
        } catch (IOException | JsonParseException e) {
            throw new DataAccessException("Veri dosyası okunamadı: " + file + " (" + e.getMessage() + ")", e);
        }
    }

    /** Listeyi JSON dizisi olarak dosyaya yazar (klasör yoksa oluşturur). */
    public static void writeList(Path file, List<?> items) {
        try {
            Path parent = file.toAbsolutePath().getParent();
            if (parent != null) Files.createDirectories(parent);
            try (Writer writer = Files.newBufferedWriter(file, StandardCharsets.UTF_8)) {
                GSON.toJson(items, writer);
            }
        } catch (IOException e) {
            throw new DataAccessException("Veri dosyasına yazılamadı: " + file + " (" + e.getMessage() + ")", e);
        }
    }
}
