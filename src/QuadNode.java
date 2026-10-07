import java.util.LinkedList;

/**
 * Uzol Quad stromu. Reprezentuje obdĺžnikovú oblasť, ktorej štyria synovia
 * predstavujú jej kvadranty:
 * 0. severozápad, 1. severovýchod, 2. juhovýchod, 3. juhozápad.
 * (Sever = väčšie hodnoty v dimenzii 1, východ = väčšie hodnoty v dimenzii 0.)
 *
 * Uzol obsahuje:
 * - POLE so 4 referenciami na potomkov (null = prázdna oblasť, alebo list),
 * - súradnice oblasti (dolná a horná hranica),
 * - LinkedList so záznamami nachádzajúcimi sa v oblasti.
 *
 * List stromu reprezentuje jeden záznam (alebo viac, ak sa oblasť už nedá
 * deliť)
 * alebo referenciu na prázdnu oblasť.
 *
 * Pamäťová náročnosť: O(1) na uzol + O(r) na r záznamov v zozname.
 * Pole potomkov sa vytvára až pri delení oblasti, list ho nedrží.
 *
 * @param <T> typ dát uložených v uzle
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
     * @param lowerBound dolná hranica oblasti (minimá v oboch dimenziách)
     * @param upperBound horná hranica oblasti (maximá v oboch dimenziách)
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

    /** List nemá vytvorené pole potomkov. */
    boolean isLeaf() {
        return children == null;
    }

    /** Vytvorí pole so 4 referenciami na potomkov (všetky prázdne = null). */
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
