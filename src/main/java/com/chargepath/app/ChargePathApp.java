package com.chargepath.app;

import com.chargepath.data.CityRepository;
import com.chargepath.data.DataAccessException;
import com.chargepath.data.JsonCityRepository;
import com.chargepath.data.JsonStationRepository;
import com.chargepath.data.JsonTripHistoryRepository;
import com.chargepath.data.JsonVehicleRepository;
import com.chargepath.data.StationRepository;
import com.chargepath.data.TripHistoryRepository;
import com.chargepath.data.VehicleRepository;
import com.chargepath.energy.EnergyModel;
import com.chargepath.energy.PhysicsEnergyModel;
import com.chargepath.routing.RoutePlanner;

import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.Scanner;

/**
 * Programın giriş noktası (main metodu).
 *
 * Tüm nesneler yalnızca burada oluşturulur ve kurucular aracılığıyla birbirine bağlanır.
 * Diğer sınıflar bağımlılıklarını arayüz olarak alır (DIP). Örneğin JSON yerine
 * veritabanı kullanılması yalnızca bu sınıfta değişiklik gerektirir.
 *
 * Çalıştırma:
 *   menü          : ChargePathApp
 *   yalnızca demo : ChargePathApp --demo
 */
public class ChargePathApp {

    public static void main(String[] args) {
        // Türkçe karakterlerin konsolda doğru görünmesi için UTF-8 kullanılır
        PrintStream out = new PrintStream(System.out, true, StandardCharsets.UTF_8);
        Path dataDir = Path.of("data");

        try {
            // 1) Veri depoları (JSON dosyaları)
            VehicleRepository vehicles = new JsonVehicleRepository(dataDir.resolve("vehicles_fallback.json"));
            StationRepository stations = new JsonStationRepository(dataDir.resolve("stations_fallback.json"));
            CityRepository cities = new JsonCityRepository(dataDir.resolve("cities.json"));
            TripHistoryRepository history = new JsonTripHistoryRepository(dataDir.resolve("trip_history.json"));

            // 2) Hesaplama: enerji modeli ve rota planlayıcı
            EnergyModel energyModel = new PhysicsEnergyModel();
            RoutePlanner planner = new RoutePlanner(stations, energyModel);

            // 3) Konsol: giriş, çıktı ve menü
            Scanner scanner = new Scanner(System.in, StandardCharsets.UTF_8);
            InputReader input = new InputReader(scanner, out);
            RoutePrinter printer = new RoutePrinter(out);
            ConsoleMenu menu = new ConsoleMenu(vehicles, stations, cities, history, energyModel, planner, input, printer, out);

            // --demo parametresiyle menü atlanır ve doğrudan demo senaryosu çalışır
            if (args.length > 0 && args[0].equals("--demo")) {
                menu.runDemo();
            } else {
                menu.run();
            }
        } catch (DataAccessException e) {
            // Veri dosyası bulunamazsa program sonlanmadan önce yönlendirici mesaj gösterilir
            out.println("Veri hatası: " + e.getMessage());
            out.println("Programı proje klasöründen (data/ klasörünün bulunduğu yerden) çalıştırdığınızdan emin olun.");
        }
    }
}
