package stromy;

/**
 * Komparator pre Quad strom s K-d stromom vo vrcholoch. SpĂˇja dve funkcie:
 * - {@link QuadComparator}: porovnanie a delenie oblasti na polovicu
 * (pre Quad strom),
 * - {@link KDComparator}: porovnanie v zadanej dimenzii (pre K-d strom
 * v kaĹľdom vrchole).
 *
 * Obe rozhrania majĂş rovnakĂş metĂłdu compare(a, b, dimension), takĹľe staÄŤĂ­
 * implementovaĹĄ jednu funkciu porovnania pre oba stromy. Dimenzie sĂş 0 (napr.
 * X)
 * a 1 (napr. Y).
 *
 * @param <T> typ dĂˇt uloĹľenĂ˝ch v strome
 */
public interface QuadKDComparator<T> extends QuadComparator<T>, KDComparator<T> {
}

