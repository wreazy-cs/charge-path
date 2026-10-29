package com.chargepath.app;

import com.chargepath.data.CityRepository;
import com.chargepath.data.StationRepository;
import com.chargepath.data.TripHistoryRepository;
import com.chargepath.data.TripRecord;
import com.chargepath.data.VehicleRepository;
import com.chargepath.energy.EnergyModel;
import com.chargepath.model.ChargingStation;
import com.chargepath.model.ElectricVehicle;
import com.chargepath.model.Location;
import com.chargepath.model.Route;
import com.chargepath.model.TripConditions;
import com.chargepath.routing.CheapestStrategy;
import com.chargepath.routing.FastestStrategy;
import com.chargepath.routing.RoutePlanner;
import com.chargepath.routing.RouteStrategy;
import com.chargepath.routing.TripRequest;

import java.io.PrintStream;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

/**
 * Kullanıcıyla etkileşimi yöneten konsol menüsü.
 * Hesaplama yapmaz; girdileri toplar, planlayıcıya iletir ve sonucu RoutePrinter ile gösterir.
 */
public class ConsoleMenu {

    // Sayıların Türkçe biçimde yazılması için (ondalık ayracı virgül: 12,5)
    private static final Locale TR = Locale.forLanguageTag("tr-TR");

    private final VehicleRepository vehicles;
    private final StationRepository stations;
    private final CityRepository cities;
    private final TripHistoryRepository history;
    private final EnergyModel energyModel;
    private final RoutePlanner planner;
    private final InputReader input;
    private final RoutePrinter printer;
    private final PrintStream out;

    public ConsoleMenu(VehicleRepository vehicles, StationRepository stations, CityRepository cities,
                       TripHistoryRepository history, EnergyModel energyModel, RoutePlanner planner,
                       InputReader input, RoutePrinter printer, PrintStream out) {
        this.vehicles = vehicles;
        this.stations = stations;
        this.cities = cities;
        this.history = history;
        this.energyModel = energyModel;
        this.planner = planner;
        this.input = input;
        this.printer = printer;
        this.out = out;
    }

    // Ana döngü: kullanıcı 0 seçene kadar menü tekrar gösterilir
    public void run() {
        printBanner();
        while (true) {
            printMenu();
            int choice = input.readInt("Seçiminiz: ", 0, 7);
            if (choice == 0) {
                out.println("İyi yolculuklar!");
                return;
            }
            try {
                switch (choice) {
                    case 1 -> listVehicles();
                    case 2 -> listStations();
                    case 3 -> calculateRange();
                    case 4 -> planRoute();
                    case 5 -> compareStrategies();
                    case 6 -> printer.printHistory(history.findAll());
                    case 7 -> runDemo();
                    default -> out.println("Geçersiz seçim.");
                }
            } catch (IllegalArgumentException e) {
                // Geçersiz istekte (örneğin aynı başlangıç ve varış) hata gösterilir, menüye dönülür
                out.println("\nHATA: " + e.getMessage());
            }
            input.waitForEnter();
        }
    }

    // Hazır demo senaryosu: kış koşullarında, %40 bataryayla Edirne'den İstanbul'a
    public void runDemo() {
        // Togg bulunamazsa listedeki ilk araç kullanılır
        ElectricVehicle togg = vehicles.findById("togg-t10x-lr").orElse(vehicles.findAll().get(0));
        Location from = findCity("Edirne");
        Location to = findCity("İstanbul (Fatih)");
        TripConditions winter = new TripConditions(110, 2, true);
        TripRequest request = new TripRequest(togg, from, to, 40, winter);

        out.println("\n DEMO: " + togg.getDisplayName() + ", %40 batarya, kış (2 °C), klima açık, 110 km/s");
        out.printf(TR, " Bu koşullarda tahmini menzil: %.0f km (%%40 bataryanın tamamı kullanılırsa)%n",
                energyModel.rangeKm(togg, request.getStartSocKwh(), winter));
        out.printf(TR, " Edirne -> İstanbul karayolu tahmini: %.0f km. Şarj olmadan yetmez.%n",
                planner.roadKm(from, to));

        // Aynı istek iki farklı stratejiyle planlanır (Strategy kalıbı)
        Optional<Route> cheapest = planner.plan(request, new CheapestStrategy());
        Optional<Route> fastest = planner.plan(request, new FastestStrategy());
        if (cheapest.isEmpty() || fastest.isEmpty()) {
            out.println(" Demo rotası bulunamadı.");
            return;
        }
        printer.printRoute(cheapest.get());
        printer.printRoute(fastest.get());
        printer.printComparison(cheapest.get(), fastest.get());
        saveToHistory(cheapest.get());
        saveToHistory(fastest.get());
    }

    // Açılış başlığı. Çok satırlı metin için text block (""") kullanılır.
    private void printBanner() {
        out.println("""

                 ____ _                            ____       _   _
                / ___| |__   __ _ _ __ __ _  ___  |  _ \\ __ _| |_| |__
               | |   | '_ \\ / _` | '__/ _` |/ _ \\ | |_) / _` | __| '_ \\
               | |___| | | | (_| | | | (_| |  __/ |  __/ (_| | |_| | | |
                \\____|_| |_|\\__,_|_|  \\__, |\\___| |_|   \\__,_|\\__|_| |_|
                                      |___/
                  Elektrikli Araç Şarj ve Rota Planlama (Trakya)
               """);
        out.printf(" Yüklenen veri: %d araç, %d istasyon, %d şehir%n",
                vehicles.findAll().size(), stations.findAll().size(), cities.findAll().size());
    }

    private void printMenu() {
        out.println("""

                ----------------- MENÜ -----------------
                 1) Araçları listele
                 2) Şarj istasyonlarını listele
                 3) Menzil hesapla
                 4) Rota planla
                 5) En ucuz ve en hızlı rotayı karşılaştır
                 6) Yolculuk geçmişi
                 7) Demo senaryosu (Edirne -> İstanbul, kış)
                 0) Çıkış
                ----------------------------------------""");
    }

    private void listVehicles() {
        out.println();
        int i = 1;
        for (ElectricVehicle v : vehicles.findAll()) {
            out.printf(TR, " %d) %-28s %-5s %5.1f kWh  %3.0f Wh/km  %4.0f kg  DC %3.0f kW  AC %2.0f kW%n",
                    i++, v.getDisplayName(), v.getTypeName(), v.getBatteryKwh(), v.getConsumptionWhPerKm(),
                    v.getMassKg(), v.getMaxDcKw(), v.getMaxAcKw());
        }
    }

    private void listStations() {
        out.println();
        out.printf(" %-7s %-34s %-22s %-3s %5s %8s%n", "Kod", "Ad", "Şehir", "Tip", "kW", "TL/kWh");
        for (ChargingStation s : stations.findAll()) {
            out.printf(TR, " %-7s %-34s %-22s %-3s %5.0f %8.2f%n",
                    s.getId(), shorten(s.getName(), 34), s.getCity(), s.getTypeName(), s.getPowerKw(), s.getPricePerKwh());
        }
        out.println("\n Not: Konumlar simülasyon için işaretlenmiştir, gerçek istasyonların varlığı garanti edilmez.");
    }

    // Seçilen koşullardaki menzili ideal koşullardaki menzille karşılaştırır
    private void calculateRange() {
        ElectricVehicle vehicle = chooseVehicle();
        double soc = input.readDouble("Batarya yüzdesi (1-100): ", 1, 100);
        TripConditions conditions = readConditions();
        double availableKwh = vehicle.getBatteryKwh() * soc / 100.0;
        double range = energyModel.rangeKm(vehicle, availableKwh, conditions);
        double ideal = energyModel.rangeKm(vehicle, availableKwh, new TripConditions(90, 20, false));
        out.printf(TR, "%n %s, %%%.0f batarya (%.1f kWh)%n", vehicle.getDisplayName(), soc, availableKwh);
        out.printf(TR, " Bu koşullarda (%s) tahmini menzil: %.0f km%n", conditions, range);
        out.printf(TR, " Karşılaştırma: 90 km/s, 20 °C, klima kapalı olsaydı: %.0f km%n", ideal);
    }

    private void planRoute() {
        TripRequest request = readTripRequest();
        out.println("\n Strateji: 1) En ucuz   2) En hızlı");
        // Seçime göre strateji nesnesi oluşturulur; planlayıcı değişmez
        RouteStrategy strategy = input.readInt("Seçiminiz: ", 1, 2) == 1 ? new CheapestStrategy() : new FastestStrategy();
        Optional<Route> route = planner.plan(request, strategy);
        if (route.isEmpty()) {
            printNoRoute();
            return;
        }
        printer.printRoute(route.get());
        saveToHistory(route.get());
    }

    private void compareStrategies() {
        TripRequest request = readTripRequest();
        Optional<Route> cheapest = planner.plan(request, new CheapestStrategy());
        Optional<Route> fastest = planner.plan(request, new FastestStrategy());
        if (cheapest.isEmpty() || fastest.isEmpty()) {
            printNoRoute();
            return;
        }
        printer.printComparison(cheapest.get(), fastest.get());
        if (input.readYesNo("\nAyrıntılı rotaları görmek ister misiniz?")) {
            printer.printRoute(cheapest.get());
            printer.printRoute(fastest.get());
        }
    }

    // Rota için gerekli bilgiler kullanıcıdan sırayla alınır
    private TripRequest readTripRequest() {
        ElectricVehicle vehicle = chooseVehicle();
        out.println("\n Başlangıç şehri:");
        Location from = chooseCity();
        out.println("\n Varış şehri:");
        Location to = chooseCity();
        double soc = input.readDouble("Başlangıç batarya yüzdesi (1-100): ", 1, 100);
        TripConditions conditions = readConditions();
        return new TripRequest(vehicle, from, to, soc, conditions);
    }

    private TripConditions readConditions() {
        double speed = input.readDouble("Ortalama hız (km/s, 30-140): ", 30, 140);
        double temperature = input.readDouble("Dış sıcaklık (°C, -20 ile 45): ", -20, 45);
        boolean climate = input.readYesNo("Klima/ısıtma açık mı?");
        return new TripConditions(speed, temperature, climate);
    }

    private ElectricVehicle chooseVehicle() {
        List<ElectricVehicle> list = vehicles.findAll();
        out.println();
        for (int i = 0; i < list.size(); i++) {
            out.printf(" %d) %s%n", i + 1, list.get(i));
        }
        // Menü 1'den, liste indeksi 0'dan başladığı için 1 çıkarılır
        return list.get(input.readInt("Araç seçin: ", 1, list.size()) - 1);
    }

    private Location chooseCity() {
        List<Location> list = cities.findAll();
        for (int i = 0; i < list.size(); i++) {
            out.printf(" %2d) %s%n", i + 1, list.get(i).getName());
        }
        return list.get(input.readInt("Şehir seçin: ", 1, list.size()) - 1);
    }

    private Location findCity(String name) {
        for (Location city : cities.findAll()) {
            if (city.getName().equals(name)) return city;
        }
        throw new IllegalArgumentException("Şehir bulunamadı: " + name);
    }

    // Rota özet kayda çevrilir ve trip_history.json dosyasına eklenir
    private void saveToHistory(Route route) {
        history.save(TripRecord.from(route));
        out.println(" (Yolculuk geçmişe kaydedildi.)");
    }

    private void printNoRoute() {
        out.println("\n Rota bulunamadı: başlangıç bataryası ya da yoldaki istasyonlar bu yolculuk için yetmiyor.");
        out.println(" İpucu: batarya yüzdesini artırın, hızı düşürün ya da klimayı kapatın.");
    }

    // Uzun adlar tablo düzenini bozmaması için kısaltılır
    private static String shorten(String text, int max) {
        return text.length() <= max ? text : text.substring(0, max - 1) + ".";
    }
}
