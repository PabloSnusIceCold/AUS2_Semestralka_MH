import java.io.BufferedReader;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * Nacitanie CSV suborov (oddelovac ';', prvy riadok je hlavicka).
 * Cisla s ciarkou namiesto bodky sa upravia na bodku, prazdne riadky sa
 * preskocia. Subory su ulozene v projekte v adresari src/data.
 */
public class DataLoader {

    public static final String NODES_PATH = "src/data/slovakia_nodes.csv";
    public static final String AREAS_PATH = "src/data/testovacie_oblasti.csv";

    /**
     * Nacita body zo suboru s formatom id;lat;lon. V bode je x = lon a y = lat.
     * Body zostavaju v poradi zo suboru.
     */
    public static List<GpsPoint> loadNodes() {
        List<GpsPoint> points = new ArrayList<>();
        try (BufferedReader reader = Files.newBufferedReader(Paths.get(NODES_PATH), StandardCharsets.UTF_8)) {
            reader.readLine(); // hlavicka
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.isBlank()) {
                    continue;
                }
                String[] parts = line.split(";");
                long id = Long.parseLong(parts[0].trim());
                double lat = parseDouble(parts[1]);
                double lon = parseDouble(parts[2]);
                points.add(new GpsPoint(id, lon, lat));
            }
        } catch (IOException e) {
            throw new UncheckedIOException("Chyba pri citani suboru " + NODES_PATH, e);
        }
        return points;
    }

    /**
     * Nacita oblasti zo suboru s formatom id;min_lat;min_lon;max_lat;max_lon.
     * Oblasti zostavaju v poradi zo suboru.
     */
    public static List<Area> loadAreas() {
        List<Area> areas = new ArrayList<>();
        try (BufferedReader reader = Files.newBufferedReader(Paths.get(AREAS_PATH), StandardCharsets.UTF_8)) {
            reader.readLine(); // hlavicka
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.isBlank()) {
                    continue;
                }
                String[] parts = line.split(";");
                long id = Long.parseLong(parts[0].trim());
                double minLat = parseDouble(parts[1]);
                double minLon = parseDouble(parts[2]);
                double maxLat = parseDouble(parts[3]);
                double maxLon = parseDouble(parts[4]);
                areas.add(new Area(id, minLon, minLat, maxLon, maxLat));
            }
        } catch (IOException e) {
            throw new UncheckedIOException("Chyba pri citani suboru " + AREAS_PATH, e);
        }
        return areas;
    }

    /** Prevedie text na cislo; ciarku (desatinnu) nahradi bodkou. */
    private static double parseDouble(String text) {
        return Double.parseDouble(text.trim().replace(',', '.'));
    }
}
