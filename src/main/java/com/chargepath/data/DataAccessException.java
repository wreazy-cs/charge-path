package com.chargepath.data;

/**
 * Veri dosyası okunamadığında ya da yazılamadığında fırlatılan hata.
 * RuntimeException'dan türediği için her çağrıda try/catch zorunlu değildir;
 * konsol uygulaması bunu en üstte yakalayıp kullanıcıya anlaşılır mesaj gösterir.
 */
public class DataAccessException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public DataAccessException(String message) {
        super(message);
    }

    public DataAccessException(String message, Throwable cause) {
        super(message, cause);
    }
}
