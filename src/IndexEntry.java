/**
 * Polozka v zozname dostupnych priestorovych indexov (kluc a factory).
 */
public class IndexEntry {
    private final String key;
    private final IndexFactory factory;

    public IndexEntry(String key, IndexFactory factory) {
        this.key = key;
        this.factory = factory;
    }

    public String getKey() {
        return key;
    }

    public IndexFactory getFactory() {
        return factory;
    }
}
