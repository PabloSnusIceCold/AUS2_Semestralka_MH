package stromy;

// Trieda KDComparator.java bola vygenerovanĂˇ pomocou AI (Claude Sonnet 5.5)

/**
 * PorovnĂˇvacia funkcia pre K-D strom. ZabezpeÄŤuje univerzĂˇlnosĹĄ kÄľĂşÄŤa:
 * strom nepoznĂˇ vnĂştornĂş ĹˇtruktĂşru dĂˇt typu T, iba ich porovnĂˇva v danej
 * dimenzii.
 *
 * @param <T> typ dĂˇt uloĹľenĂ˝ch v strome
 */
@FunctionalInterface
public interface KDComparator<T> {
    /**
     * PorovnĂˇ dva objekty v zadanej dimenzii (0 .. k-1).
     *
     * @return zĂˇpornĂş hodnotu, nulu alebo kladnĂş hodnotu ak je a menĹˇĂ­,
     *         rovnĂ˝ alebo vĂ¤ÄŤĹˇĂ­ ako b v dimenzii dimension
     */
    int compare(T a, T b, int dimension);
}

