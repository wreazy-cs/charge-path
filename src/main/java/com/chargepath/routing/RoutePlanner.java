package com.chargepath.routing;

import com.chargepath.data.StationRepository;
import com.chargepath.energy.EnergyModel;
import com.chargepath.model.ChargingStation;
import com.chargepath.model.ElectricVehicle;
import com.chargepath.model.Location;
import com.chargepath.model.Route;
import com.chargepath.model.RouteLeg;
import com.chargepath.model.TripConditions;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.PriorityQueue;

/**
 * Batarya kısıtlı Dijkstra algoritmasıyla rota planlar.
 *
 * Yerler: başlangıç (0), varış (1) ve şarj istasyonları (2, 3, ...)
 *
 * Klasik Dijkstra yalnızca konumu dikkate alır. Elektrikli araçta batarya
 * seviyesi de belirleyicidir; bu nedenle durum YER + BATARYA YÜZDESİ olarak
 * tanımlanır. Harita, her biri bir batarya yüzdesine karşılık gelen katlar
 * şeklinde düşünülebilir (%0, %1, ... %100).
 *
 *     %80 kat:  ...  [Lüleburgaz DC] ...
 *        ^ şarj (üst kata çıkış)
 *     %22 kat:  ...  [Lüleburgaz DC] ...
 *        ^ sürüş (enerji harcanır, alt kata iniş)
 *     %50 kat:  [Kırklareli] ...
 *
 * İki tür adım vardır:
 *   Sürüş : bir yerden diğerine geçiş. Varışta bataryada en az %10 kalmıyorsa adım geçersizdir.
 *   Şarj  : istasyonda bataryanın en fazla %80'e yükseltilmesi. Lityum iyon batarya
 *           %80'den sonra belirgin şekilde yavaş dolar.
 *
 * Batarya seviyesi %1'lik basamaklara aşağı yuvarlanır (güvenli taraf):
 * %37,6 ile varılan nokta %37 olarak değerlendirilir.
 *
 * Sınıf yalnızca rota planlar; dosya okuma ve ekrana yazma yapmaz (SRP).
 * İstasyon deposu ve enerji modeli kurucu üzerinden arayüz olarak alınır (DIP).
 */
public class RoutePlanner {

    // Kuş uçuşu mesafeyi karayolu mesafesine çeviren katsayı (Trakya için varsayım)
    public static final double DEFAULT_ROAD_FACTOR = 1.3;
    // Varışta bataryada kalması gereken en düşük yüzde (yedek pay)
    public static final double DEFAULT_RESERVE_PERCENT = 10;
    // Şarjda ulaşılabilecek en yüksek yüzde (şarj üst sınırı)
    public static final double DEFAULT_CHARGE_LIMIT_PERCENT = 80;

    private static final int LEVELS = 101;   // batarya seviyeleri: %0 .. %100
    private static final int START = 0;
    private static final int DESTINATION = 1;
    private static final int ARRIVED = 0;    // yere varıldı, henüz şarj yapılmadı
    private static final int READY = 1;      // yola çıkmaya hazır

    private final StationRepository stationRepository;
    private final EnergyModel energyModel;
    private final double roadFactor;
    private final double reservePercent;
    private final double chargeLimitPercent;

    // Varsayılan değerlerle oluşturur; asıl kurucu this(...) ile çağrılır
    public RoutePlanner(StationRepository stationRepository, EnergyModel energyModel) {
        this(stationRepository, energyModel, DEFAULT_ROAD_FACTOR, DEFAULT_RESERVE_PERCENT, DEFAULT_CHARGE_LIMIT_PERCENT);
    }

    public RoutePlanner(StationRepository stationRepository, EnergyModel energyModel,
                        double roadFactor, double reservePercent, double chargeLimitPercent) {
        if (stationRepository == null || energyModel == null) {
            throw new IllegalArgumentException("İstasyon deposu ve enerji modeli boş olamaz");
        }
        if (roadFactor < 1) throw new IllegalArgumentException("Yol katsayısı 1'den küçük olamaz");
        if (reservePercent < 0 || reservePercent >= chargeLimitPercent || chargeLimitPercent > 100) {
            throw new IllegalArgumentException("Yedek pay, şarj sınırından küçük olmalı; sınır en fazla %100 olmalı");
        }
        this.stationRepository = stationRepository;
        this.energyModel = energyModel;
        this.roadFactor = roadFactor;
        this.reservePercent = reservePercent;
        this.chargeLimitPercent = chargeLimitPercent;
    }

    // Kuş uçuşu mesafeyi tahmini karayolu mesafesine çevirir
    public double roadKm(Location from, Location to) {
        return from.distanceKmTo(to) * roadFactor;
    }

    public double getReservePercent() {
        return reservePercent;
    }

    public double getChargeLimitPercent() {
        return chargeLimitPercent;
    }

    /**
     * En iyi rotayı bulur. Rota yoksa null yerine boş Optional döner;
     * böylece çağıran tarafta NullPointerException oluşmaz.
     */
    public Optional<Route> plan(TripRequest request, RouteStrategy strategy) {
        ElectricVehicle vehicle = request.getVehicle();
        TripConditions conditions = request.getConditions();
        List<ChargingStation> stations = stationRepository.findAll();
        int n = stations.size() + 2; // istasyonlar + başlangıç + varış

        // 1. ADIM: Tüm yerler tek bir dizide toplanır
        Location[] places = new Location[n];
        places[START] = request.getFrom();
        places[DESTINATION] = request.getTo();
        for (int i = 0; i < stations.size(); i++) places[i + 2] = stations.get(i).getLocation();

        // Her yer çifti için mesafe ve enerji bir kez hesaplanıp tabloda tutulur.
        // Dijkstra aynı değerlere tekrar tekrar eriştiği için gereksiz hesap önlenir.
        double[][] km = new double[n][n];
        double[][] energy = new double[n][n];
        for (int u = 0; u < n; u++) {
            for (int v = 0; v < n; v++) {
                if (u == v) continue;
                km[u][v] = roadKm(places[u], places[v]);
                energy[u][v] = energyModel.legEnergyKwh(vehicle, places[u], places[v], km[u][v], conditions);
            }
        }

        double capacity = vehicle.getBatteryKwh();
        double stepKwh = capacity / (LEVELS - 1);           // %1'in kWh karşılığı
        double reserveKwh = capacity * reservePercent / 100.0;
        int limitLevel = (int) Math.floor(chargeLimitPercent);

        // 2. ADIM: Dijkstra tabloları. Her durum (yer, seviye, aşama) tek bir sayıyla gösterilir.
        int stateCount = n * LEVELS * 2;
        double[] best = new double[stateCount];          // duruma ulaşmanın en düşük ağırlığı
        int[] previous = new int[stateCount];            // duruma hangi durumdan gelindiği
        double[] arrivalKwh = new double[stateCount];    // varışta kalan gerçek enerji (yuvarlanmamış)
        boolean[] done = new boolean[stateCount];        // durum kesinleşti mi
        Arrays.fill(best, Double.POSITIVE_INFINITY);     // başlangıçta tüm durumlar sonsuz uzaklıkta
        Arrays.fill(previous, -1);

        int startLevel = (int) Math.floor(request.getStartSocPercent());
        int startState = state(START, startLevel, READY);
        best[startState] = 0;
        PriorityQueue<QueueEntry> queue = new PriorityQueue<>(); // en düşük ağırlıklı durum önce çıkar
        queue.add(new QueueEntry(startState, 0));
        int goal = -1;

        // 3. ADIM: Ana döngü
        while (!queue.isEmpty()) {
            QueueEntry current = queue.poll();
            int s = current.state;
            if (done[s]) continue;            // daha iyi bir kaydı işlenmiş durum atlanır
            done[s] = true;

            int place = placeOf(s);
            int level = levelOf(s);
            int phase = phaseOf(s);

            if (place == DESTINATION) {       // varış kuyruktan ilk çıktığında en iyi rota bulunmuştur
                goal = s;
                break;
            }

            if (phase == READY) {
                // SÜRÜŞ: diğer tüm yerler denenir (başlangıca dönüş anlamsız olduğu için v = 1'den başlar)
                double departKwh = (place == START) ? request.getStartSocKwh() : level * stepKwh;
                for (int v = 1; v < n; v++) {
                    if (v == place) continue;
                    double arrival = departKwh - energy[place][v];
                    if (arrival < reserveKwh) continue;          // batarya yetersiz, adım geçersiz
                    int arrivalLevel = Math.min(LEVELS - 1, (int) Math.floor(arrival / stepKwh + 1e-9));
                    int next = state(v, arrivalLevel, ARRIVED);
                    double hours = km[place][v] / conditions.getSpeedKmh();
                    double weight = strategy.driveWeight(km[place][v], hours);
                    if (relax(best, previous, queue, s, next, weight)) {
                        arrivalKwh[next] = arrival;
                    }
                }
            } else {
                // ŞARJ: mevcut seviyeden üst sınıra kadar her seviye denenir
                ChargingStation station = stations.get(place - 2);
                for (int target = level + 1; target <= limitLevel; target++) {
                    double charged = (target - level) * stepKwh;
                    double cost = station.chargingCost(charged);
                    double hours = station.chargingTimeHours(charged, vehicle);
                    relax(best, previous, queue, s, state(place, target, READY), strategy.chargeWeight(charged, cost, hours));
                }
            }
        }

        // Varışa ulaşılamadıysa rota yoktur
        if (goal < 0) {
            return Optional.empty();
        }
        return Optional.of(buildRoute(request, strategy, stations, places, km, energy, stepKwh, previous, arrivalKwh, goal));
    }

    // Yeni yol mevcut kayıttan iyiyse tablo güncellenir ve durum kuyruğa eklenir (relaxation)
    private boolean relax(double[] best, int[] previous, PriorityQueue<QueueEntry> queue,
                          int from, int to, double weight) {
        double candidate = best[from] + weight;
        if (candidate < best[to]) {
            best[to] = candidate;
            previous[to] = from;
            queue.add(new QueueEntry(to, candidate));
            return true;
        }
        return false;
    }

    // previous dizisi varıştan geriye izlenerek rota oluşturulur
    private Route buildRoute(TripRequest request, RouteStrategy strategy, List<ChargingStation> stations,
                             Location[] places, double[][] km, double[][] energy, double stepKwh,
                             int[] previous, double[] arrivalKwh, int goal) {
        // Durumlar geriye doğru toplandığı için liste ters çevrilir
        List<Integer> path = new ArrayList<>();
        for (int s = goal; s != -1; s = previous[s]) path.add(s);
        Collections.reverse(path);

        ElectricVehicle vehicle = request.getVehicle();
        double speed = request.getConditions().getSpeedKmh();
        List<RouteLeg> legs = new ArrayList<>();

        // path sırası: READY(başlangıç) -> ARRIVED(istasyon) -> READY(istasyon) -> ... -> ARRIVED(varış)
        int i = 0;
        while (i < path.size() - 1) {
            int departState = path.get(i);
            int arriveState = path.get(i + 1);
            int u = placeOf(departState);
            int v = placeOf(arriveState);
            double depart = (u == START) ? request.getStartSocKwh() : levelOf(departState) * stepKwh;
            double arrival = arrivalKwh[arriveState];

            ChargingStation station = null;
            double charged = 0;
            double cost = 0;
            double chargeHours = 0;
            int nextIndex = i + 1;
            // Varılan yer istasyonsa sonraki adım şarjdır ve aynı bacağa eklenir
            if (v != DESTINATION && i + 2 < path.size()) {
                station = stations.get(v - 2);
                int chargedState = path.get(i + 2);
                charged = (levelOf(chargedState) - levelOf(arriveState)) * stepKwh;
                cost = station.chargingCost(charged);
                chargeHours = station.chargingTimeHours(charged, vehicle);
                nextIndex = i + 2;
            }
            legs.add(new RouteLeg(places[u], places[v], station, km[u][v], energy[u][v], depart, arrival,
                    charged, cost, km[u][v] / speed, chargeHours));
            i = nextIndex;
        }
        return new Route(vehicle, request.getConditions(), strategy.getName(), legs);
    }

    // Durum numarası ile (yer, seviye, aşama) arasında dönüşüm yapan yardımcı metotlar.
    // Üç bilginin tek sayıda tutulması dizi kullanımını kolaylaştırır.

    private static int state(int place, int level, int phase) {
        return (place * LEVELS + level) * 2 + phase;
    }

    private static int placeOf(int state) {
        return state / 2 / LEVELS;
    }

    private static int levelOf(int state) {
        return (state / 2) % LEVELS;
    }

    private static int phaseOf(int state) {
        return state % 2;
    }

    // Öncelik kuyruğu kaydı: durum ve o ana kadarki toplam ağırlık.
    // Comparable sayesinde kuyruk en düşük ağırlığı önce verir.
    private static final class QueueEntry implements Comparable<QueueEntry> {
        private final int state;
        private final double weight;

        private QueueEntry(int state, double weight) {
            this.state = state;
            this.weight = weight;
        }

        @Override
        public int compareTo(QueueEntry other) {
            return Double.compare(weight, other.weight);
        }
    }
}
