package model;

// Trieda GpsPoint.java bola vygenerovaná pomocou AI modelu Gemini 3.8 Flash

/**
 * GPS bod. Pozor na poradie: x = lon (zemepisna dlzka), y = lat (zemepisna sirka).
 */
public class GpsPoint {
    private final long id;
    private final double x; // lon
    private final double y; // lat

    public GpsPoint(long id, double x, double y) {
        this.id = id;
        this.x = x;
        this.y = y;
    }

    public long getId() {
        return id;
    }

    /** @return lon */
    public double getX() {
        return x;
    }

    /** @return lat */
    public double getY() {
        return y;
    }

    @Override
    public String toString() {
        return "GpsPoint{id=" + id + ", x(lon)=" + x + ", y(lat)=" + y + "}";
    }
}

