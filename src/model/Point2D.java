package model;

// Trieda Point2D.java bola vygenerovanĂˇ pomocou AI (Claude Sonnet 5.5)

/**
 * JednoduchĂ˝ 2D bod pouĹľĂ­vanĂ˝ testerom. Identita objektu je dĂ´leĹľitĂˇ: tester
 * porovnĂˇva vĂ˝sledky Find s referenÄŤnĂ˝mi vĂ˝sledkami po konkrĂ©tnych objektoch
 * (==), nie iba podÄľa sĂşradnĂ­c alebo poÄŤtu. Preto trieda zĂˇmerne neprepisuje
 * equals/hashCode.
 *
 * Pole id je poradovĂ© ÄŤĂ­slo vloĹľenĂ©ho zĂˇznamu v referenÄŤnom zozname.
 * HraniÄŤnĂ© body intervalov pouĹľĂ­vajĂş id = -1.
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

