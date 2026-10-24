package model;

// Trieda Area.java bola vygenerovaná pomocou AI modelu Gemini 3.8 Flash

/**
 * Obdĺžniková oblasť zadaná v GPS súradniciach.
 * minX = min_lon, minY = min_lat, maxX = max_lon, maxY = max_lat.
 */
public class Area {
    private final long id;
    private final double minX;
    private final double minY;
    private final double maxX;
    private final double maxY;

    public Area(long id, double minX, double minY, double maxX, double maxY) {
        this.id = id;
        this.minX = minX;
        this.minY = minY;
        this.maxX = maxX;
        this.maxY = maxY;
    }

    public long getId() {
        return id;
    }

    public double getMinX() {
        return minX;
    }

    public double getMinY() {
        return minY;
    }

    public double getMaxX() {
        return maxX;
    }

    public double getMaxY() {
        return maxY;
    }

    @Override
    public String toString() {
        return "Area{id=" + id + ", minX(min_lon)=" + minX + ", minY(min_lat)=" + minY
                + ", maxX(max_lon)=" + maxX + ", maxY(max_lat)=" + maxY + "}";
    }
}

