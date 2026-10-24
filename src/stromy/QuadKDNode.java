package stromy;

// Trieda QuadKDNode.java bola vygenerovaná pomocou AI modelu Gemini 3.8 Flash

import java.util.List;

/**
 * Uzol Quad stromu, ktorý záznamy uchováva v K-d strome (nie v zozname).
 * Reprezentuje obdĺžnikovú oblasť, ktorej štyria synovia predstavujú kvadranty:
 * 0. severozápad, 1. severovýchod, 2. juhovýchod, 3. juhozápad.
 * (Sever = väčšie hodnoty v dimenzii 1, východ = väčšie hodnoty v dimenzii 0.)
 *
 * Uzol obsahuje:
 * - POLE so 4 referenciami na potomkov (null = prázdna oblasť, alebo list),
 * - súradnice oblasti (dolná a horná hranica),
 * - K-d strom so záznamami nachádzajúcimi sa v oblasti.
 *
 * K-d strom sa vytvára až pri vložení prvého záznamu a po rozdelení oblasti
 * sa zruší, takže vnútorné uzly a prázdne oblasti ho nedržia.
 * Uzol si pamätá aj prvý vložený záznam (firstRecord) na rýchle porovnanie
 * kľúča pri vkladaní (O(1) bez prehľadávania K-d stromu).
 *
 * Pamäťová náročnosť: O(1) na uzol + O(r) na r záznamov v K-d strome.
 *
 * @param <T> typ dát uložených v uzle
 */
class QuadKDNode<T> {
    static final int NORTH_WEST = 0;
    static final int NORTH_EAST = 1;
    static final int SOUTH_EAST = 2;
    static final int SOUTH_WEST = 3;

    static final int CHILD_COUNT = 4;

    private static final int DIMENSIONS = 2;

    private final T lowerBound;
    private final T upperBound;
    private final KDComparator<T> comparator;
    private KDTree<T> records;
    private T firstRecord;
    private QuadKDNode<T>[] children;

    /**
     * @param lowerBound dolná hranica oblasti (minimá v oboch dimenziách)
     * @param upperBound horná hranica oblasti (maximá v oboch dimenziách)
     * @param comparator funkcia porovnávajúca dáta v K-d strome uzla
     */
    QuadKDNode(T lowerBound, T upperBound, KDComparator<T> comparator) {
        this.lowerBound = lowerBound;
        this.upperBound = upperBound;
        this.comparator = comparator;
    }

    T getLowerBound() {
        return lowerBound;
    }

    T getUpperBound() {
        return upperBound;
    }

    /** Uzol neobsahuje žiadny záznam. */
    boolean hasNoRecords() {
        return records == null;
    }

    /** Prvý záznam vložený do uzla (null, ak uzol nemá záznamy). */
    T getFirstRecord() {
        return firstRecord;
    }

    /**
     * Pridá záznam do K-d stromu uzla.
     * Časová zložitosť: O(h) vloženia do K-d stromu, kde h je jeho výška.
     */
    void addRecord(T data) {
        if (records == null) {
            records = new KDTree<>(DIMENSIONS, comparator);
            firstRecord = data;
        }
        records.insert(data);
    }

    /**
     * Nájde záznamy uzla ležiace v zadanej oblasti pomocou intervalového
     * vyhľadávania v K-d strome uzla (bez prehľadania všetkých záznamov).
     */
    List<T> findRecords(T lower, T upper) {
        return records.find(lower, upper);
    }

    /**
     * Presunie všetky záznamy uzla do iného uzla a vyprázdni tento uzol.
     * Predpoklad: všetky záznamy uzla majú rovnaký kľúč (su to duplicity) ako
     * firstRecord (tak je to vždy pri uzle, ktorý sa ešte môže deliť), preto ich
     * K-d strom nájde jedným intervalovým vyhľadávaním na tomto kľúči.
     */
    void moveRecordsTo(QuadKDNode<T> target) {
        for (T record : records.find(firstRecord, firstRecord)) {
            target.addRecord(record);
        }
        records = null;
        firstRecord = null;
    }

    /** List nemá vytvorené pole potomkov. */
    boolean isLeaf() {
        return children == null;
    }

    /** Vytvorí pole so 4 referenciami na potomkov (všetky prázdne = null). */
    @SuppressWarnings("unchecked")
    void createChildren() {
        children = (QuadKDNode<T>[]) new QuadKDNode[CHILD_COUNT];
    }

    /** @param quadrant NORTH_WEST, NORTH_EAST, SOUTH_EAST alebo SOUTH_WEST */
    QuadKDNode<T> getChild(int quadrant) {
        return children == null ? null : children[quadrant];
    }

    void setChild(int quadrant, QuadKDNode<T> child) {
        children[quadrant] = child;
    }
}
