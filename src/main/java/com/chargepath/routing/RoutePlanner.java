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
 * Batarya kısıtlı Dijkstra ile rota planlayıcı.
 *
 * DÜĞÜMLER (yerler): başlangıç (0), varış (1), her şarj istasyonu (2, 3, ...)
 *
 * DURUM = YER + BATARYA SEVİYESİ
 *   Sadece "neredeyim?" yetmez, "ne kadar bataryam var?" da önemli. Bu yüzden
 *   haritayı kat kat düşünürüz: her kat bir batarya yüzdesi (%0, %1, ... %100).
 *   Dijkstra bu katlı haritada en düşük ağırlıklı yolu arar.
 *
 *     %80 kat:  ...  [Lüleburgaz DC] ...
 *        ^ şarj (yukarı çık)
 *     %22 kat:  ...  [Lüleburgaz DC] ...
 *        ^ sür (aşağı in, yol enerji yer)
 *     %50 kat:  [Kırklareli] ...
 *
 * İKİ ÇEŞİT ADIM (kenar)
 *   Sür   : bir yerden diğerine. Batarya azalır. Varışta yedek pay (%10) kalmıyorsa
 *           bu kenar yok sayılır (kısıt budur).
 *   Şarj  : bir istasyonda daha yüksek bir yüzdeye çık (en fazla %80, çünkü DC şarj
 *           %80'den sonra belirgin yavaşlar). Bir istasyona gelince en az bir kez
 *           şarj edilmeden oradan devam edilmez (boşuna uğramak anlamsız).
 *
 * Batarya %1'lik basamaklara AŞAĞI yuvarlanır (güvenli taraf): %37.6 ile
 * varılırsa planlayıcı %37 varmış gibi davranır.
 *
 * Tek sorumluluk (SRP): bu sınıf sadece rota planlar. Veri okumaz, ekrana yazmaz.
 * Bağımlılıklar kurucudan arayüz olarak gelir (DIP).
 */
public class RoutePlanner {

    /** Kuş uçuşu mesafeyi karayolu mesafesine çeviren katsayı (Trakya için varsayım). */
    public static final double DEFAULT_ROAD_FACTOR = 1.3;
    public static final double DEFAULT_RESERVE_PERCENT = 10;
    public static final double DEFAULT_CHARGE_LIMIT_PERCENT = 80;

    private static final int LEVELS = 101;   // %0 .. %100
    private static final int START = 0;
    private static final int DESTINATION = 1;
    private static final int ARRIVED = 0;    // yere yeni varıldı (istasyonda henüz şarj edilmedi)
    private static final int READY = 1;      // yola çıkmaya hazır

    private final StationRepository stationRepository;
    private final EnergyModel energyModel;
    private final double roadFactor;
    private final double reservePercent;
    private final double chargeLimitPercent;

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

    /** Kuş uçuşu mesafeyi karayolu tahminine çevirir. */
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
     * En iyi rotayı bulur. Hiç rota yoksa boş Optional döner; null dönmeyiz,
     * böylece çağıran taraf NullPointerException yaşamaz.
     */
    public Optional<Route> plan(TripRequest request, RouteStrategy strategy) {
        ElectricVehicle vehicle = request.getVehicle();
        TripConditions conditions = request.getConditions();
        List<ChargingStation> stations = stationRepository.findAll();
        int n = stations.size() + 2;

        // 1) Her yer çifti için yol ve enerji bir kez hesaplanır (sonra tekrar tekrar kullanılır).
        Location[] places = new Location[n];
        places[START] = request.getFrom();
        places[DESTINATION] = request.getTo();
        for (int i = 0; i < stations.size(); i++) places[i + 2] = stations.get(i).getLocation();

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

        // 2) Dijkstra tabloları. Her durum tek bir sayıyla adreslenir.
        int stateCount = n * LEVELS * 2;
        double[] best = new double[stateCount];
        int[] previous = new int[stateCount];
        double[] arrivalKwh = new double[stateCount];   // sürüşle gelinen durumlarda gerçek (yuvarlanmamış) varış enerjisi
        boolean[] done = new boolean[stateCount];
        Arrays.fill(best, Double.POSITIVE_INFINITY);
        Arrays.fill(previous, -1);

        int startLevel = (int) Math.floor(request.getStartSocPercent());
        int startState = state(START, startLevel, READY);
        best[startState] = 0;
        PriorityQueue<QueueEntry> queue = new PriorityQueue<>();
        queue.add(new QueueEntry(startState, 0));
        int goal = -1;

        while (!queue.isEmpty()) {
            QueueEntry current = queue.poll();
            int s = current.state;
            if (done[s]) continue;            // aynı durumun eski (daha kötü) kaydı
            done[s] = true;

            int place = placeOf(s);
            int level = levelOf(s);
            int phase = phaseOf(s);

            if (place == DESTINATION) {       // varış kesinleşti, daha iyisi bulunamaz
                goal = s;
                break;
            }

            if (phase == READY) {
                // SÜR: buradan başka her yere (başlangıca geri dönmek anlamsız)
                double departKwh = (place == START) ? request.getStartSocKwh() : level * stepKwh;
                for (int v = 1; v < n; v++) {
                    if (v == place) continue;
                    double arrival = departKwh - energy[place][v];
                    if (arrival < reserveKwh) continue;          // batarya yetmiyor: kenar yok
                    int arrivalLevel = Math.min(LEVELS - 1, (int) Math.floor(arrival / stepKwh + 1e-9));
                    int next = state(v, arrivalLevel, ARRIVED);
                    double hours = km[place][v] / conditions.getSpeedKmh();
                    double weight = strategy.driveWeight(km[place][v], hours);
                    if (relax(best, previous, queue, s, next, weight)) {
                        arrivalKwh[next] = arrival;
                    }
                }
            } else {
                // ŞARJ: istasyonda bulunduğun seviyeden daha yükseğe çık
                ChargingStation station = stations.get(place - 2);
                for (int target = level + 1; target <= limitLevel; target++) {
                    double charged = (target - level) * stepKwh;
                    double cost = station.chargingCost(charged);
                    double hours = station.chargingTimeHours(charged, vehicle);
                    relax(best, previous, queue, s, state(place, target, READY), strategy.chargeWeight(charged, cost, hours));
                }
            }
        }

        if (goal < 0) {
            return Optional.empty();
        }
        return Optional.of(buildRoute(request, strategy, stations, places, km, energy, stepKwh, previous, arrivalKwh, goal));
    }

    /** Daha iyi bir yol bulunduysa tabloyu günceller (relaxation). */
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

    /** Varış durumundan geriye yürüyüp durumları sıralar, sonra bacaklara (RouteLeg) çevirir. */
    private Route buildRoute(TripRequest request, RouteStrategy strategy, List<ChargingStation> stations,
                             Location[] places, double[][] km, double[][] energy, double stepKwh,
                             int[] previous, double[] arrivalKwh, int goal) {
        List<Integer> path = new ArrayList<>();
        for (int s = goal; s != -1; s = previous[s]) path.add(s);
        Collections.reverse(path);

        ElectricVehicle vehicle = request.getVehicle();
        double speed = request.getConditions().getSpeedKmh();
        List<RouteLeg> legs = new ArrayList<>();

        // path: READY(başlangıç) -> ARRIVED(istasyon) -> READY(istasyon) -> ARRIVED ... -> ARRIVED(varış)
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

    // --- Durum numarası <-> (yer, seviye, aşama) dönüşümleri ---

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

    /** Öncelik kuyruğundaki bir kayıt: durum ve o ana kadarki ağırlık. Küçük ağırlık önce çıkar. */
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
