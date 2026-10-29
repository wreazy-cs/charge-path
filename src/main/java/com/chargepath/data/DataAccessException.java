package com.chargepath.data;

/**
 * Veri dosyası okunamadığında ya da yazılamadığında fırlatılan özel istisna sınıfı.
 * RuntimeException'dan türediği için her çağrıda try/catch zorunlu değildir.
 * main metodunda yakalanır ve kullanıcıya anlaşılır bir mesaj gösterilir.
 */
public class DataAccessException extends RuntimeException {

    // İstisna sınıfları Serializable olduğu için sürüm numarası tanımlanır
    private static final long serialVersionUID = 1L;

    public DataAccessException(String message) {
        super(message);
    }

    // cause: asıl hata (örneğin IOException) da saklanır
    public DataAccessException(String message, Throwable cause) {
        super(message, cause);
    }
}
