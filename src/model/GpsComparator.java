package model;

import stromy.QuadKDComparator;
/**
 * Komparator pre GpsPoint (x = lon, y = lat), pouzitelny v K-d strome,
 * Quad strome aj Quad strome s K-d stromom vo vrcholoch.
 * Volitelne pocita volania compare (staticke pocitadlo calls).
 */
public class GpsComparator implements QuadKDComparator<GpsPoint> {
    public static boolean counting = false;
    public static long calls = 0;

    @Override
    public int compare(GpsPoint a, GpsPoint b, int d) {
        if (counting) {
            calls++;
        }
        return d == 0 ? Double.compare(a.getX(), b.getX()) : Double.compare(a.getY(), b.getY());
    }

    @Override
    public GpsPoint middle(GpsPoint lower, GpsPoint upper) {
        return new GpsPoint(-1, lower.getX() + (upper.getX() - lower.getX()) / 2,
                lower.getY() + (upper.getY() - lower.getY()) / 2);
    }

    @Override
    public GpsPoint combine(GpsPoint xSource, GpsPoint ySource) {
        return new GpsPoint(-1, xSource.getX(), ySource.getY());
    }
}

