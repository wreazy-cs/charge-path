package com.chargepath.app;

import com.chargepath.data.TripRecord;
import com.chargepath.model.ChargingStation;
import com.chargepath.model.ElectricVehicle;
import com.chargepath.model.Route;
import com.chargepath.model.RouteLeg;

import java.io.PrintStream;
import java.util.List;
import java.util.Locale;

/**
 * Tüm ekran çıktıları bu sınıfta toplanır (Tek Sorumluluk ilkesi, SRP).
 * Planlayıcı hesaplar, bu sınıf yalnızca sonucu gösterir.
 */
public class RoutePrinter {

    private static final Locale TR = Locale.forLanguageTag("tr-TR");
    private static final int BAR_WIDTH = 20; // batarya çubuğunun karakter uzunluğu

    private final PrintStream out;

    public RoutePrinter(PrintStream out) {
        this.out = out;
    }

    // Rotayı bacak bacak yazdırır, sonunda toplamları gösterir
    public void printRoute(Route route) {
        ElectricVehicle v = route.getVehicle();
        double capacity = v.getBatteryKwh();

        out.println();
        line('=');
        out.printf(TR, " ROTA (%s): %s  ->  %s%n", route.getStrategyName(), route.getStart(), route.getDestination());
        out.printf(TR, " Araç: %s | Koşullar: %s%n", v.getDisplayName(), route.getConditions());
        line('=');

        int step = 1;
        for (RouteLeg leg : route.getLegs()) {
            out.printf(TR, "%n %d) %s  ->  %s%n", step++, leg.getFrom(), leg.getTo());
            out.printf(TR, "    Yol: %.1f km | Enerji: %.1f kWh | Sürüş: %s%n",
                    leg.getRoadKm(), leg.getEnergyKwh(), formatHours(leg.getDriveHours()));
            out.printf(TR, "    Çıkış  %s%n", batteryBar(leg.getDepartSocKwh(), capacity));
            out.printf(TR, "    Varış  %s%n", batteryBar(leg.getArrivalSocKwh(), capacity));
            // Bacak istasyonda bitiyorsa şarj bilgisi de yazdırılır
            if (leg.endsAtStation()) {
                ChargingStation s = leg.getStation();
                out.printf(TR, "    ŞARJ: %s %s (%s %.0f kW, %.2f TL/kWh)%n",
                        s.getId(), s.getName(), s.getTypeName(), s.getPowerKw(), s.getPricePerKwh());
                out.printf(TR, "          +%.1f kWh, %s, %.2f TL%n",
                        leg.getChargedKwh(), formatHours(leg.getChargeHours()), leg.getChargeCost());
            }
        }

        out.println();
        line('-');
        out.printf(TR, " Toplam yol      : %.1f km%n", route.getTotalDistanceKm());
        out.printf(TR, " Harcanan enerji : %.1f kWh%n", route.getTotalEnergyKwh());
        out.printf(TR, " Şarj durakları  : %d (yüklenen %.1f kWh)%n", route.getStopCount(), route.getTotalChargedKwh());
        out.printf(TR, " Şarj ücreti     : %.2f TL%n", route.getTotalCost());
        out.printf(TR, " Toplam süre     : %s (sürüş %s + şarj %s)%n", formatHours(route.getTotalHours()),
                formatHours(route.getTotalDriveHours()), formatHours(route.getTotalChargeHours()));
        out.printf(TR, " Varışta batarya : %s%n", batteryBar(route.getFinalSocKwh(), capacity));
        line('-');
    }

    // En ucuz ve en hızlı rotayı karşılaştırma tablosu olarak gösterir
    public void printComparison(Route cheapest, Route fastest) {
        out.println();
        line('=');
        out.println(" KARŞILAŞTIRMA");
        line('=');
        out.printf(TR, " %-18s %14s %14s%n", "", cheapest.getStrategyName(), fastest.getStrategyName());
        out.printf(TR, " %-18s %14s %14s%n", "Şarj ücreti", money(cheapest.getTotalCost()), money(fastest.getTotalCost()));
        out.printf(TR, " %-18s %14s %14s%n", "Toplam süre", formatHours(cheapest.getTotalHours()), formatHours(fastest.getTotalHours()));
        out.printf(TR, " %-18s %14d %14d%n", "Durak sayısı", cheapest.getStopCount(), fastest.getStopCount());
        out.printf(TR, " %-18s %14s %14s%n", "Yol", km(cheapest.getTotalDistanceKm()), km(fastest.getTotalDistanceKm()));
        line('-');
        double savedMoney = fastest.getTotalCost() - cheapest.getTotalCost();
        double savedTime = cheapest.getTotalHours() - fastest.getTotalHours();
        out.printf(TR, " En ucuz rota %.2f TL tasarruf ettirir, en hızlı rota %s kazandırır.%n",
                savedMoney, formatHours(savedTime));
    }

    public void printHistory(List<TripRecord> records) {
        if (records.isEmpty()) {
            out.println("\n Henüz kayıtlı yolculuk yok.");
            return;
        }
        out.println();
        line('=');
        out.println(" YOLCULUK GEÇMİŞİ (" + records.size() + " kayıt)");
        line('=');
        int i = 1;
        for (TripRecord r : records) {
            out.printf(TR, " %d) %s | %s | %s -> %s | %s%n", i++, r.getDate(), r.getVehicle(), r.getFrom(), r.getTo(), r.getStrategy());
            out.printf(TR, "    %.1f km, %.1f kWh, %d durak, %.2f TL, %s%n",
                    r.getDistanceKm(), r.getEnergyKwh(), r.getStopCount(), r.getCost(), formatHours(r.getHours()));
            for (String stop : r.getStops()) {
                out.println("      - " + stop);
            }
        }
    }

    // Batarya çubuğu. Örnek: [██████████░░░░░░░░░░]  50% (42.5 kWh)
    public String batteryBar(double socKwh, double capacityKwh) {
        // Yüzde 0 ile 100 arasında sınırlanır
        double percent = Math.max(0, Math.min(100, socKwh / capacityKwh * 100));
        // Dolu karakter sayısı hesaplanır
        int filled = (int) Math.round(percent / 100 * BAR_WIDTH);
        String bar = "█".repeat(filled) + "░".repeat(BAR_WIDTH - filled);
        return String.format(TR, "[%s] %3.0f%% (%.1f kWh)", bar, percent, socKwh);
    }

    // Ondalık saati okunur biçime çevirir: 1,75 saat -> "1 sa 45 dk"
    public static String formatHours(double hours) {
        long totalMinutes = Math.round(hours * 60);
        long h = totalMinutes / 60;
        long m = totalMinutes % 60;
        if (h == 0) return m + " dk";
        return h + " sa " + m + " dk";
    }

    private static String money(double tl) {
        return String.format(TR, "%.2f TL", tl);
    }

    private static String km(double value) {
        return String.format(TR, "%.1f km", value);
    }

    // 64 karakterlik ayırıcı çizgi yazdırır
    private void line(char c) {
        out.println(String.valueOf(c).repeat(64));
    }
}
