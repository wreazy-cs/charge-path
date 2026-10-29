package com.chargepath.routing;

/**
 * Strateji (Strategy) tasarım kalıbı: "Bir adımın bedeli nedir?" sorusunun cevabı.
 *
 * Dijkstra algoritması hep aynı kalır, sadece kenar ağırlığını bu arayüz verir.
 * Algoritmada iki çeşit adım var: sürmek ve şarj etmek.
 *
 *   CheapestStrategy -> sürmek neredeyse bedava, şarj = ödenen TL
 *   FastestStrategy  -> sürmek = sürüş saati, şarj = şarj saati + durak kaybı
 *
 * Yeni bir strateji (örn. "en az durak") eklemek için RoutePlanner'a dokunmadan
 * yeni bir sınıf yazmak yeterlidir (Açık/Kapalı İlkesi, OCP).
 * Ağırlıklar negatif olmamalı, Dijkstra buna dayanır.
 */
public interface RouteStrategy {

    String getName();

    /** Bir noktadan diğerine sürmenin ağırlığı. */
    double driveWeight(double roadKm, double driveHours);

    /** Bir istasyonda şarj etmenin ağırlığı. */
    double chargeWeight(double chargedKwh, double cost, double chargeHours);
}
