package adaptery;

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
     * NĂˇjde vsetky body v oblasti ohranicenej bodmi lower a upper
     * (hranice vratane). Vracia povodne objekty (nie kopie).
     */
    List<GpsPoint> find(GpsPoint lower, GpsPoint upper);

    /** Pocet porovnani kluca od nastavenia pocitadla; -1 ak index porovnania nepocita. */
    default long comparisons() {
        return -1;
    }
}

