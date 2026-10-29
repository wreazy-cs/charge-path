# ChargePath: Elektrikli Araç Şarj ve Rota Planlama

Kırklareli Üniversitesi Yazılım Mühendisliği, **Nesneye Yönelik Programlama** dersi dönem projesi.

ChargePath, elektrikli bir aracın kalan bataryasıyla ne kadar mesafe gidebileceğini hesaplayan ve Trakya bölgesindeki şarj istasyonlarını kullanarak **en ucuz** ya da **en hızlı** rotayı bulan bir Java konsol uygulamasıdır.

```
 DEMO: Togg T10X Long Range RWD, %40 batarya, kış (2 °C), klima açık, 110 km/s

                           En ucuz       En hızlı
 Şarj ücreti             581,02 TL      840,99 TL
 Toplam süre            7 sa 53 dk     2 sa 56 dk
 Durak sayısı                    2              2
```

## Özellikler

| Menü | Açıklama |
|---|---|
| 1. Araçları listele | 4 araç: Togg T10X, Togg T10F, Tesla Model Y, Tesla Model 3 |
| 2. İstasyonları listele | Trakya'da 28 şarj istasyonu (17 DC, 11 AC) |
| 3. Menzil hesapla | Batarya, hız, sıcaklık ve klimaya göre tahmini menzil |
| 4. Rota planla | Seçilen stratejiye göre şarj duraklı rota |
| 5. Karşılaştır | Aynı yolculuk için iki stratejinin karşılaştırması |
| 6. Yolculuk geçmişi | Planlanan rotalar dosyaya kaydedilir ve program kapatıldığında korunur |
| 7. Demo | Edirne'den İstanbul'a, kış koşullarında, %40 bataryayla hazır senaryo |

## Kurulum ve çalıştırma

Gereksinimler: **JDK 21** ve Maven (IntelliJ IDEA ile birlikte gelir). İnternet bağlantısı gerekmez; tüm veriler `data/` klasöründedir.

**IntelliJ IDEA:**

1. Projeyi açın ve Maven panelinden **Reload** yapın.
2. `app/ChargePathApp.java` dosyasını çalıştırın.
3. Testler için `src/test/java` klasörüne sağ tıklayıp **Run 'All Tests'** seçin.

**Komut satırı:**

```bash
mvn test                                   # 30 birim testi
mvn compile exec:java                      # menülü uygulama
mvn compile exec:java -Dexec.args="--demo" # yalnızca demo
```

> Veriler `data/` klasöründen okunduğu için program proje kök klasöründen çalıştırılmalıdır.

## Proje yapısı

```
charge-path/
├── data/       araç, istasyon ve şehir verileri (JSON)
├── docs/       UML diyagramları
└── src/
    ├── main/java/com/chargepath/
    │   ├── model/     araç, istasyon, konum, rota
    │   ├── energy/    enerji hesabı
    │   ├── routing/   rota planlama (Dijkstra) ve stratejiler
    │   ├── data/      JSON okuma/yazma, fabrikalar
    │   └── app/       konsol menüsü ve main
    └── test/java/     30 birim testi
```

Proje **37 sınıf ve arayüzden** oluşur. Her paketin tek bir sorumluluğu vardır: `app` kullanıcıyla etkileşimi, `routing` rota kararını, `energy` enerji hesabını, `data` dosya işlemlerini, `model` ise temel nesneleri içerir.

## UML sınıf diyagramları

Diyagram okunabilirlik için iki parçaya ayrılmıştır. Ayrıntılı inceleme için **PDF sürümü** önerilir.

### 1. Model: araç, istasyon, rota

[PDF](docs/uml-model.pdf) · [SVG](docs/uml-model.svg)

![Model UML](docs/uml-model.png)

### 2. Mimari: paketler, arayüzler, kalıplar

Renkler: mavi `app`, sarı `routing`, yeşil `energy`, mor `data`, gri `model`.

[PDF](docs/uml-mimari.pdf) · [SVG](docs/uml-mimari.svg)

![Mimari UML](docs/uml-mimari.png)

## Nesneye yönelik programlama ilkeleri

| İlke | Uygulama |
|---|---|
| **Kapsülleme** | Tüm alanlar `private` tanımlıdır. Batarya seviyesi yalnızca `consume()` ve `charge()` ile değişir; bu metotlar seviyenin 0'ın altına düşmesini ve kapasiteyi aşmasını engeller. |
| **Kalıtım** | `Sedan` ve `Suv`, `ElectricVehicle` sınıfından; `AcStation` ve `DcFastStation`, `ChargingStation` sınıfından türer. |
| **Soyutlama** | `ElectricVehicle` ve `ChargingStation` soyut sınıftır; `EnergyModel`, `RouteStrategy` ve depolar arayüzdür. |
| **Çok biçimlilik** | `effectivePowerKw()` AC ve DC istasyonda farklı hesaplanır; `getDragFactor()` sedan ve SUV için farklı değer döndürür. |

## SOLID ilkeleri

| İlke | Uygulama |
|---|---|
| **S** Tek sorumluluk | `RoutePlanner` yalnızca rota planlar. Ekran çıktısı `RoutePrinter`, klavye girişi `InputReader` tarafından yapılır. |
| **O** Açık/kapalı | Yeni bir strateji, `RoutePlanner` değiştirilmeden yeni bir sınıfla eklenebilir. |
| **L** Liskov yerine geçme | Planlayıcı AC ve DC istasyonlarla aynı şekilde çalışır; kodda `instanceof` kontrolü yoktur. |
| **I** Arayüz ayrımı | Arayüzler küçük tutulmuştur: `EnergyModel` 2, `RouteStrategy` 3 metot içerir. |
| **D** Bağımlılığın tersine çevrilmesi | `RoutePlanner` somut sınıflara değil arayüzlere bağlıdır. Tüm nesneler `main` içinde oluşturulur; bu sayede testlerde sahte depo kullanılabilir. |

## Tasarım kalıpları

| Kalıp | Sınıflar | Amaç |
|---|---|---|
| **Strategy** | `CheapestStrategy`, `FastestStrategy` | Aynı algoritmanın farklı maliyet tanımlarıyla çalışması |
| **Factory** | `VehicleFactory`, `StationFactory` | JSON'daki tipe göre uygun alt sınıfın üretilmesi |
| **Repository** | `*Repository` arayüzleri | Veri kaynağının programın geri kalanından ayrılması |

## Algoritma: batarya kısıtlı Dijkstra

Klasik Dijkstra en kısa yolu bulur; elektrikli araçta ise bataryanın yol boyunca yeterli kalması da gerekir. Bu nedenle her durum **yer ve batarya yüzdesi** birlikte ele alınarak tanımlanır.

```
  %80   [Lüleburgaz DC] ──── sürüş ────► [İstanbul]   (varışta en az %10)
              ▲ şarj
  %22   [Lüleburgaz DC]
              ▲ sürüş (batarya azalır)
  %40   [Edirne]
```

- **Sürüş:** Bir yerden diğerine geçiştir. Varışta bataryada en az **%10 yedek** kalmıyorsa geçersizdir.
- **Şarj:** İstasyonda batarya en fazla **%80'e** yükseltilir. Lityum iyon batarya %80'den sonra belirgin şekilde yavaş dolar.
- **Ağırlık:** En ucuz rotada ödenen ücret, en hızlı rotada sürüş süresi, şarj süresi ve durak başına 5 dakikadır.

Mesafe, **Haversine** formülüyle hesaplanan kuş uçuşu değerin 1,3 ile çarpılmasıyla elde edilir.

## Enerji modeli

```
 Toplam = Sürüş + Klima + Yokuş - Rejeneratif kazanç
```

| Etken | Etki |
|---|---|
| Hız | Hava direnci hızın karesiyle artar; yüksek hızda tüketim belirgin şekilde yükselir. |
| Araç tipi | SUV'un hava direnci sedana göre %15 fazla kabul edilmiştir. |
| Sıcaklık | 20 °C altında derece başına %0,5 ek tüketim uygulanır. |
| Klima | Isıtma, soğutmaya göre daha fazla enerji gerektirir (1 ile 4 kW arası). |
| Eğim | Tırmanışta enerji harcanır, inişte rejeneratif frenle %60'ı geri kazanılır. |

## Veriler

| Dosya | İçerik |
|---|---|
| `vehicles_fallback.json` | 4 aracın batarya, tüketim, ağırlık ve şarj gücü değerleri |
| `stations_fallback.json` | 28 istasyonun konum, tip, güç ve fiyat bilgisi |
| `cities.json` | 12 şehir merkezi |
| `trip_history.json` | Çalışma sırasında oluşturulur, planlanan yolculukları içerir |

JSON işlemleri için Gson kütüphanesi kullanılır. Uygulama tamamen çevrimdışı çalışır.

> **Not:** İstasyon konumları simülasyon amacıyla belirlenmiştir; gerçek istasyonların varlığı ve güncelliği garanti edilmez. Bazı güç ve fiyat değerleri varsayımdır ve ilgili kaydın `notes` alanında belirtilmiştir.

## Birim testleri

`mvn test` komutuyla **30 test** çalışır (JUnit 5).

| Test sınıfı | Kapsam |
|---|---|
| `LocationTest` (4) | Mesafe hesabı ve geçersiz koordinat |
| `ElectricVehicleTest` (6) | Batarya kuralları ve çok biçimlilik |
| `ChargingStationTest` (3) | AC/DC şarj gücü, süre ve ücret |
| `PhysicsEnergyModelTest` (5) | Hız, eğim, sıcaklık ve klimanın etkisi |
| `VehicleFactoryTest` (3) | Doğru araç tipinin üretilmesi |
| `JsonRepositoryTest` (4) | Dosya okuma, eksik dosya ve kalıcılık |
| `RoutePlannerTest` (5) | Duraksız rota, zorunlu şarj, rota bulunamaması, yedek pay ve stratejiler |

## Varsayımlar

- Karayolu mesafesi, kuş uçuşu mesafenin 1,3 ile çarpılmasıyla tahmin edilir.
- DC şarj gücü sabit kabul edilir (en yüksek gücün %85'i).
- Trafik, rüzgâr ve istasyonda bekleme süresi hesaba katılmaz.
- Yalnızca yolculuk sırasında ödenen şarj ücreti hesaplanır.

## Kaynaklar

- İstasyon konumlarının bir kısmı: © [OpenStreetMap](https://www.openstreetmap.org/copyright) katkıda bulunanları (ODbL lisansı)
- Rakım verileri: SRTM 90 m ([OpenTopoData](https://www.opentopodata.org/))
- Araç değerleri: [ev-database.org](https://ev-database.org), [evkx.net](https://evkx.net)
- Şarj fiyatları: [chargemate.io](https://chargemate.io) ve piyasa ortalaması

## Geliştirici

Samet Muhammed Ali Şafak ([github.com/wreazy-cs](https://github.com/wreazy-cs))
