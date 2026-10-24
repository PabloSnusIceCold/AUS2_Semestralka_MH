package stromy;

// Trieda QuadComparator.java bola vygenerovanĂˇ pomocou AI (Claude Sonnet 5.5)

/**
 * Funkcia (rozhranie) zabezpeÄŤujĂşca univerzĂˇlnosĹĄ kÄľĂşÄŤa pre Quad strom.
 * Strom nepoznĂˇ vnĂştornĂş ĹˇtruktĂşru dĂˇt typu T. VyĹľaduje od nej iba tri
 * operĂˇcie:
 * porovnanie, rozdelenie oblasti na polovicu a skombinovanie dvoch objektov.
 *
 * Quad strom je dvojrozmernĂ˝, preto sa pouĹľĂ­vajĂş iba dimenzie
 * 0 (napr. X) a 1 (napr. Y).
 *
 * @param <T> typ dĂˇt uloĹľenĂ˝ch v strome
 */
public interface QuadComparator<T> {
    /**
     * PorovnĂˇ dva objekty v zadanej dimenzii (0 alebo 1).
     *
     * @return zĂˇpornĂş hodnotu, nulu alebo kladnĂş hodnotu ak je a menĹˇĂ­,
     *         rovnĂ˝ alebo vĂ¤ÄŤĹˇĂ­ ako b v dimenzii dimension
     */
    int compare(T a, T b, int dimension);

    /**
     * Rozdelenie oblasti na polovicu: vrĂˇti objekt typu T, ktorĂ©ho hodnota
     * v kaĹľdej dimenzii leĹľĂ­ v strede medzi hodnotami lower a upper
     * (lower + (upper - lower) / 2).
     */
    T middle(T lower, T upper);

    /**
     * VytvorĂ­ objekt typu T, ktorĂ©ho hodnota v dimenzii 0 je prevzatĂˇ z objektu
     * xSource a v dimenzii 1 z objektu ySource. PouĹľĂ­va sa na zostavenie rohov
     * oblastĂ­ kvadrantov pri delenĂ­ oblasti.
     */
    T combine(T xSource, T ySource);
}

