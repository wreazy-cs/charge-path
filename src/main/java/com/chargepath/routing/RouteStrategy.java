package com.chargepath.routing;

/**
 * Strategy tasarım kalıbı: rota adımlarının maliyetini (ağırlığını) belirler.
 *
 * Dijkstra algoritması değişmez; yalnızca adım ağırlıkları bu arayüzden gelir.
 * Algoritmada iki tür adım vardır: sürüş ve şarj.
 *
 *   CheapestStrategy : sürüş neredeyse ücretsiz, şarj = ödenen TL
 *   FastestStrategy  : sürüş = sürüş süresi, şarj = şarj süresi + durak kaybı
 *
 * Yeni bir strateji, RoutePlanner değiştirilmeden yeni bir sınıfla eklenebilir
 * (Açık/Kapalı ilkesi, OCP). Dijkstra gereği ağırlıklar negatif olmamalıdır.
 */
public interface RouteStrategy {

    // Ekranda gösterilecek ad ("En ucuz", "En hızlı")
    String getName();

    // İki nokta arasındaki sürüşün ağırlığı
    double driveWeight(double roadKm, double driveHours);

    // İstasyonda yapılan şarjın ağırlığı
    double chargeWeight(double chargedKwh, double cost, double chargeHours);
}
