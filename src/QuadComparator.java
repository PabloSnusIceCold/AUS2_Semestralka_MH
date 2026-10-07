/**
 * Funkcia (rozhranie) zabezpečujúca univerzálnosť kľúča pre Quad strom.
 * Strom nepozná vnútornú štruktúru dát typu T. Vyžaduje od nej iba tri operácie:
 * porovnanie, rozdiel hodnôt a rozdelenie oblasti na polovicu.
 *
 * Quad strom je dvojrozmerný, preto sa používajú iba dimenzie
 * 0 (napr. X) a 1 (napr. Y).
 *
 * @param <T> typ dát uložených v strome
 */
public interface QuadComparator<T> {
    /**
     * Porovná dva objekty v zadanej dimenzii (0 alebo 1).
     *
     * @return zápornú hodnotu, nulu alebo kladnú hodnotu ak je a menší,
     *         rovný alebo väčší ako b v dimenzii dimension
     */
    int compare(T a, T b, int dimension);

    /**
     * Rozdiel hodnôt dvoch kľúčov v zadanej dimenzii (b - a).
     * Používa sa na zistenie veľkosti oblasti, a teda aj na rozhodnutie,
     * či je ďalšie delenie oblasti ešte možné.
     */
    double difference(T a, T b, int dimension);

    /**
     * Rozdelenie oblasti na polovicu: vráti objekt typu T, ktorého hodnota
     * v každej dimenzii leží v strede medzi hodnotami lower a upper
     * (lower + (upper - lower) / 2).
     */
    T middle(T lower, T upper);

    /**
     * Vytvorí objekt typu T, ktorého hodnota v dimenzii 0 je prevzatá z objektu
     * xSource a v dimenzii 1 z objektu ySource. Používa sa na zostavenie rohov
     * oblastí kvadrantov pri delení oblasti.
     */
    T combine(T xSource, T ySource);
}
