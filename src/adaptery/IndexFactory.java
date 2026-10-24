package adaptery;

/**
 * Vytvara prazdny priestorovy index pre zadany rozsah suradnic.
 */
@FunctionalInterface
public interface IndexFactory {
    SpatialIndex create(double minX, double minY, double maxX, double maxY);
}
