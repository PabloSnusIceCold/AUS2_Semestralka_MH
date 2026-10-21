/**
 * Komparator pre Quad strom s K-d stromom vo vrcholoch. Spája dve funkcie:
 * - {@link QuadComparator}: porovnanie a delenie oblasti na polovicu
 * (pre Quad strom),
 * - {@link KDComparator}: porovnanie v zadanej dimenzii (pre K-d strom
 * v každom vrchole).
 *
 * Obe rozhrania majú rovnakú metódu compare(a, b, dimension), takže stačí
 * implementovať jednu funkciu porovnania pre oba stromy. Dimenzie sú 0 (napr.
 * X)
 * a 1 (napr. Y).
 *
 * @param <T> typ dát uložených v strome
 */
public interface QuadKDComparator<T> extends QuadComparator<T>, KDComparator<T> {
}
