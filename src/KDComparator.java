/**
 * Porovnávacia funkcia pre K-D strom. Zabezpečuje univerzálnosť kľúča:
 * strom nepozná vnútornú štruktúru dát typu T, iba ich porovnáva v danej dimenzii.
 *
 * @param <T> typ dát uložených v strome
 */
@FunctionalInterface
public interface KDComparator<T> {
    /**
     * Porovná dva objekty v zadanej dimenzii (0 .. k-1).
     *
     * @return zápornú hodnotu, nulu alebo kladnú hodnotu ak je a menší,
     *         rovný alebo väčší ako b v dimenzii dimension
     */
    int compare(T a, T b, int dimension);
}
