package com.chargepath.app;

import java.io.PrintStream;
import java.util.Scanner;

/**
 * Klavyeden güvenli değer okuma işlemlerini yapar.
 * Hatalı girişte program sonlanmaz, değer yeniden istenir.
 * Ondalık sayılarda hem "12.5" hem "12,5" kabul edilir.
 */
public class InputReader {

    private final Scanner scanner;
    private final PrintStream out;

    public InputReader(Scanner scanner, PrintStream out) {
        this.scanner = scanner;
        this.out = out;
    }

    // min ile max arasında geçerli bir tam sayı girilene kadar tekrar sorar
    public int readInt(String prompt, int min, int max) {
        while (true) {
            out.print(prompt);
            String line = nextLine();
            try {
                int value = Integer.parseInt(line.trim());
                if (value >= min && value <= max) return value;
            } catch (NumberFormatException ignored) {
                // Sayı girilmediyse uyarı gösterilir ve tekrar sorulur
            }
            out.printf("  Lütfen %d ile %d arasında bir tam sayı girin.%n", min, max);
        }
    }

    // Ondalık sayı okur. Java ondalık ayracı olarak nokta beklediği için virgül dönüştürülür.
    public double readDouble(String prompt, double min, double max) {
        while (true) {
            out.print(prompt);
            String line = nextLine().trim().replace(',', '.');
            try {
                double value = Double.parseDouble(line);
                if (value >= min && value <= max) return value;
            } catch (NumberFormatException ignored) {
                // tekrar sorulur
            }
            out.printf("  Lütfen %.0f ile %.0f arasında bir sayı girin.%n", min, max);
        }
    }

    // Evet/hayır sorusu. "e", "evet", "h", "hayır" ve "hayir" kabul edilir.
    public boolean readYesNo(String prompt) {
        while (true) {
            out.print(prompt + " (e/h): ");
            String line = nextLine().trim().toLowerCase();
            if (line.equals("e") || line.equals("evet")) return true;
            if (line.equals("h") || line.equals("hayır") || line.equals("hayir")) return false;
            out.println("  Lütfen 'e' ya da 'h' girin.");
        }
    }

    // Sonucun ekranda kalması için Enter tuşu beklenir
    public void waitForEnter() {
        out.print("\nDevam etmek için Enter'a basın...");
        nextLine();
    }

    private String nextLine() {
        if (!scanner.hasNextLine()) {
            throw new IllegalStateException("Giriş akışı kapandı");
        }
        return scanner.nextLine();
    }
}
