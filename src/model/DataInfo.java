package model;

import java.util.List;

/**
 * Nacita CSV subory a vypise zakladne informacie o datach.
 * Subory sa citaju z adresara src/data (cesty su v DataLoader).
 * Pouzitie: java DataInfo (spustat z korenoveho adresara projektu)
 */
public class DataInfo {

    public static void main(String[] args) {
        List<GpsPoint> points = DataLoader.loadNodes();
        List<Area> areas = DataLoader.loadAreas();

        System.out.println("Pocet bodov: " + points.size());
        System.out.println("Pocet oblasti: " + areas.size());

        if (!points.isEmpty()) {
            double minLat = Double.MAX_VALUE;
            double maxLat = -Double.MAX_VALUE;
            double minLon = Double.MAX_VALUE;
            double maxLon = -Double.MAX_VALUE;
            for (GpsPoint p : points) {
                minLat = Math.min(minLat, p.getY());
                maxLat = Math.max(maxLat, p.getY());
                minLon = Math.min(minLon, p.getX());
                maxLon = Math.max(maxLon, p.getX());
            }
            System.out.println("lat: min = " + minLat + ", max = " + maxLat);
            System.out.println("lon: min = " + minLon + ", max = " + maxLon);
        }

        System.out.println("Prve body:");
        for (int i = 0; i < Math.min(3, points.size()); i++) {
            System.out.println("  " + points.get(i));
        }
        System.out.println("Prve oblasti:");
        for (int i = 0; i < Math.min(3, areas.size()); i++) {
            System.out.println("  " + areas.get(i));
        }
    }
}

