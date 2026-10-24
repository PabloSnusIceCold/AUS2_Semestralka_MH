package stromy;

// Trieda QuadNode.java bola vygenerovanĂˇ pomocou AI (Claude Sonnet 5.5)

import java.util.LinkedList;

/**
 * Uzol Quad stromu. Reprezentuje obdÄşĹľnikovĂş oblasĹĄ, ktorej Ĺˇtyria synovia
 * predstavujĂş jej kvadranty:
 * 0. severozĂˇpad, 1. severovĂ˝chod, 2. juhovĂ˝chod, 3. juhozĂˇpad.
 * (Sever = vĂ¤ÄŤĹˇie hodnoty v dimenzii 1, vĂ˝chod = vĂ¤ÄŤĹˇie hodnoty v dimenzii 0.)
 *
 * Uzol obsahuje:
 * - POLE so 4 referenciami na potomkov (null = prĂˇzdna oblasĹĄ, alebo list),
 * - sĂşradnice oblasti (dolnĂˇ a hornĂˇ hranica),
 * - LinkedList so zĂˇznamami nachĂˇdzajĂşcimi sa v oblasti.
 *
 * List stromu reprezentuje jeden zĂˇznam (alebo viac, ak sa oblasĹĄ uĹľ nedĂˇ
 * deliĹĄ)
 * alebo referenciu na prĂˇzdnu oblasĹĄ.
 *
 * PamĂ¤ĹĄovĂˇ nĂˇroÄŤnosĹĄ: O(1) na uzol + O(r) na r zĂˇznamov v zozname.
 * Pole potomkov sa vytvĂˇra aĹľ pri delenĂ­ oblasti, list ho nedrĹľĂ­.
 *
 * @param <T> typ dĂˇt uloĹľenĂ˝ch v uzle
 */
class QuadNode<T> {
    static final int NORTH_WEST = 0;
    static final int NORTH_EAST = 1;
    static final int SOUTH_EAST = 2;
    static final int SOUTH_WEST = 3;

    static final int CHILD_COUNT = 4;

    private final T lowerBound;
    private final T upperBound;
    private final LinkedList<T> records;
    private QuadNode<T>[] children;

    /**
     * @param lowerBound dolnĂˇ hranica oblasti (minimĂˇ v oboch dimenziĂˇch)
     * @param upperBound hornĂˇ hranica oblasti (maximĂˇ v oboch dimenziĂˇch)
     */
    QuadNode(T lowerBound, T upperBound) {
        this.lowerBound = lowerBound;
        this.upperBound = upperBound;
        this.records = new LinkedList<>();
    }

    T getLowerBound() {
        return lowerBound;
    }

    T getUpperBound() {
        return upperBound;
    }

    LinkedList<T> getRecords() {
        return records;
    }

    /** List nemĂˇ vytvorenĂ© pole potomkov. */
    boolean isLeaf() {
        return children == null;
    }

    /** VytvorĂ­ pole so 4 referenciami na potomkov (vĹˇetky prĂˇzdne = null). */
    @SuppressWarnings("unchecked")
    void createChildren() {
        children = (QuadNode<T>[]) new QuadNode[CHILD_COUNT];
    }

    /** @param quadrant NORTH_WEST, NORTH_EAST, SOUTH_EAST alebo SOUTH_WEST */
    QuadNode<T> getChild(int quadrant) {
        return children == null ? null : children[quadrant];
    }

    void setChild(int quadrant, QuadNode<T> child) {
        children[quadrant] = child;
    }
}

