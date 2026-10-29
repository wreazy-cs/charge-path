package com.chargepath.model;

import com.chargepath.TestData;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

// Location testleri: mesafe hesabı ve geçersiz değer kontrolü
class LocationTest {

    // Bir noktanın kendisine uzaklığı 0 olmalıdır
    @Test
    void distanceToItselfIsZero() {
        assertEquals(0.0, TestData.KIRKLARELI.distanceKmTo(TestData.KIRKLARELI), 1e-9);
    }

    // Haversine sonucu gerçek mesafeyle uyumlu olmalıdır (2 km tolerans)
    @Test
    void kirklareliToEdirneIsAbout56KmAsTheCrowFlies() {
        // Gerçek kuş uçuşu mesafe yaklaşık 56 km
        assertEquals(56, TestData.KIRKLARELI.distanceKmTo(TestData.EDIRNE), 2);
    }

    // A'dan B'ye ve B'den A'ya mesafe eşit olmalıdır
    @Test
    void distanceIsSymmetric() {
        assertEquals(TestData.KIRKLARELI.distanceKmTo(TestData.ISTANBUL),
                TestData.ISTANBUL.distanceKmTo(TestData.KIRKLARELI), 1e-9);
    }

    // 90'dan büyük enlem reddedilmelidir
    @Test
    void invalidLatitudeIsRejected() {
        assertThrows(IllegalArgumentException.class, () -> new Location("Hatalı", 95, 27, 0));
    }
}
