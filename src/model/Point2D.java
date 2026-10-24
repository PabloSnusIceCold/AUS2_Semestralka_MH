package model;

// Trieda Point2D.java bola vygenerovaná pomocou AI (Claude Sonnet 5.5)

/**
 * Jednoduchý 2D bod používaný testerom. Identita objektu je dôležitá: tester
 * porovnáva výsledky Find s referenčnými výsledkami po konkrétnych objektoch
 * (==), nie iba podľa súradníc alebo počtu. Preto trieda zámerne neprepisuje
 * equals/hashCode.
 *
 * Pole id je poradové číslo vloženého záznamu v referenčnom zozname.
 * Hraničné body intervalov používajú id = -1.
 */
public final class Point2D {
    public final int x;
    public final int y;
    public final int id;

    public Point2D(int x, int y, int id) {
        this.x = x;
        this.y = y;
        this.id = id;
    }

    @Override
    public String toString() {
        return "P#" + id + "(" + x + "," + y + ")";
    }
}

