package adaptery;

// Trieda IndexFactory.java bola vygenerovaná pomocou AI modelu Gemini 3.8 Flash

/**
 * Vytvara prazdny priestorovy index pre zadany rozsah suradnic.
 */
@FunctionalInterface
public interface IndexFactory {
    SpatialIndex create(double minX, double minY, double maxX, double maxY);
}
