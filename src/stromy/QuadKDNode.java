package stromy;

import java.util.List;

/**
 * Uzol Quad stromu, ktorĂ˝ zĂˇznamy uchovĂˇva v K-d strome (nie v zozname).
 * Reprezentuje obdÄşĹľnikovĂş oblasĹĄ, ktorej Ĺˇtyria synovia predstavujĂş kvadranty:
 * 0. severozĂˇpad, 1. severovĂ˝chod, 2. juhovĂ˝chod, 3. juhozĂˇpad.
 * (Sever = vĂ¤ÄŤĹˇie hodnoty v dimenzii 1, vĂ˝chod = vĂ¤ÄŤĹˇie hodnoty v dimenzii 0.)
 *
 * Uzol obsahuje:
 * - POLE so 4 referenciami na potomkov (null = prĂˇzdna oblasĹĄ, alebo list),
 * - sĂşradnice oblasti (dolnĂˇ a hornĂˇ hranica),
 * - K-d strom so zĂˇznamami nachĂˇdzajĂşcimi sa v oblasti.
 *
 * K-d strom sa vytvĂˇra aĹľ pri vloĹľenĂ­ prvĂ©ho zĂˇznamu a po rozdelenĂ­ oblasti
 * sa zruĹˇĂ­, takĹľe vnĂştornĂ© uzly a prĂˇzdne oblasti ho nedrĹľia.
 * Uzol si pamĂ¤tĂˇ aj prvĂ˝ vloĹľenĂ˝ zĂˇznam (firstRecord) na rĂ˝chle porovnanie
 * kÄľĂşÄŤa pri vkladanĂ­ (O(1) bez prehÄľadĂˇvania K-d stromu).
 *
 * PamĂ¤ĹĄovĂˇ nĂˇroÄŤnosĹĄ: O(1) na uzol + O(r) na r zĂˇznamov v K-d strome.
 *
 * @param <T> typ dĂˇt uloĹľenĂ˝ch v uzle
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
     * @param lowerBound dolnĂˇ hranica oblasti (minimĂˇ v oboch dimenziĂˇch)
     * @param upperBound hornĂˇ hranica oblasti (maximĂˇ v oboch dimenziĂˇch)
     * @param comparator funkcia porovnĂˇvajĂşca dĂˇta v K-d strome uzla
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

    /** Uzol neobsahuje Ĺľiadny zĂˇznam. */
    boolean hasNoRecords() {
        return records == null;
    }

    /** PrvĂ˝ zĂˇznam vloĹľenĂ˝ do uzla (null, ak uzol nemĂˇ zĂˇznamy). */
    T getFirstRecord() {
        return firstRecord;
    }

    /**
     * PridĂˇ zĂˇznam do K-d stromu uzla.
     * ÄŚasovĂˇ zloĹľitosĹĄ: O(h) vloĹľenia do K-d stromu, kde h je jeho vĂ˝Ĺˇka.
     */
    void addRecord(T data) {
        if (records == null) {
            records = new KDTree<>(DIMENSIONS, comparator);
            firstRecord = data;
        }
        records.insert(data);
    }

    /**
     * NĂˇjde zĂˇznamy uzla leĹľiace v zadanej oblasti pomocou intervalovĂ©ho
     * vyhÄľadĂˇvania v K-d strome uzla (bez prehÄľadania vĹˇetkĂ˝ch zĂˇznamov).
     */
    List<T> findRecords(T lower, T upper) {
        return records.find(lower, upper);
    }

    /**
     * Presunie vĹˇetky zĂˇznamy uzla do inĂ©ho uzla a vyprĂˇzdni tento uzol.
     * Predpoklad: vĹˇetky zĂˇznamy uzla majĂş rovnakĂ˝ kÄľĂşÄŤ (su to duplicity) ako
     * firstRecord (tak je to vĹľdy pri uzle, ktorĂ˝ sa eĹˇte mĂ´Ĺľe deliĹĄ), preto ich
     * K-d strom nĂˇjde jednĂ˝m intervalovĂ˝m vyhÄľadĂˇvanĂ­m na tomto kÄľĂşÄŤi.
     */
    void moveRecordsTo(QuadKDNode<T> target) {
        for (T record : records.find(firstRecord, firstRecord)) {
            target.addRecord(record);
        }
        records = null;
        firstRecord = null;
    }

    /** List nemĂˇ vytvorenĂ© pole potomkov. */
    boolean isLeaf() {
        return children == null;
    }

    /** VytvorĂ­ pole so 4 referenciami na potomkov (vĹˇetky prĂˇzdne = null). */
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

