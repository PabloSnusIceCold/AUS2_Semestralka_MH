package adaptery;

// Trieda SpatialIndex.java bola vygenerovaná pomocou AI modelu Gemini 3.8 Flash

import model.GpsPoint;
import java.util.List;

/**
 * Spolocne rozhranie priestorovych indexov nad GPS bodmi (Insert a Find).
 */
public interface SpatialIndex {

    /** Nazov indexu (na vypis). */
    String name();

    /** Vlozi bod do indexu. */
    void insert(GpsPoint p);

    /**
     * Nájde všetky body v oblasti ohraničenej bodmi lower a upper
     * (hranice vrátane). Vracia pôvodné objekty (nie kópie).
     */
    List<GpsPoint> find(GpsPoint lower, GpsPoint upper);

    /** Pocet porovnani kluca od nastavenia pocitadla; -1 ak index porovnania nepocita. */
    default long comparisons() {
        return -1;
    }
}

