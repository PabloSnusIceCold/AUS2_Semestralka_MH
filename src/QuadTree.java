import java.util.LinkedList;
import java.util.List;

/**
 * Univerzálny Quad strom (bez rekurzie) s operáciami Insert a Find.
 * Štýlom zodpovedá KDTree&lt;T&gt;: porovnávanie, rozdiel a delenie kľúčov
 * zabezpečuje {@link QuadComparator}.
 *
 * Koreň reprezentuje celú oblasť zadanú na začiatku (rozsah kľúčov).
 * Ak oblasť obsahuje viac ako 1 záznam, delí sa na 4 kvadranty, až kým
 * nie je v každej oblasti najviac 1 záznam, alebo kým nie je dosiahnutá
 * maximálna povolená výška stromu (maxDepth). Koreň má hĺbku 0.
 *
 * @param <T> typ dát uložených v strome
 */
public class QuadTree<T> {
    private static final int DIMENSIONS = 2;
    private final QuadComparator<T> comparator;
    private final T lowerBound;
    private final T upperBound;
    private final int maxDepth;
    private final QuadNode<T> root;

    /**
     * @param comparator funkcia porovnávajúca kľúče, počítajúca ich rozdiel
     *                   a delenie na polovicu
     * @param lowerBound dolná hranica rozsahu kľúčov (minimá v oboch dimenziách)
     * @param upperBound horná hranica rozsahu kľúčov (maximá v oboch dimenziách)
     * @param maxDepth   maximálna povolená výška stromu (hĺbka uzla, ktorý sa ešte
     *                   smie deliť, je menšia ako maxDepth); po jej dosiahnutí sa
     *                   oblasť už nedelí a záznam sa pridá do zoznamu vo vrchole
     */
    public QuadTree(QuadComparator<T> comparator, T lowerBound, T upperBound, int maxDepth) {
        if (maxDepth < 0) {
            throw new IllegalArgumentException("maxDepth musi byt >= 0");
        }
        if (comparator == null) {
            throw new IllegalArgumentException("comparator nesmie byt null");
        }
        if (lowerBound == null || upperBound == null) {
            throw new IllegalArgumentException("rozsah klucov nesmie byt null");
        }
        this.comparator = comparator;
        this.lowerBound = lowerBound;
        this.upperBound = upperBound;
        this.maxDepth = maxDepth;
        this.root = new QuadNode<>(lowerBound, upperBound);
    }

    /**
     * Vloží bod do stromu.
     */
    public void insert(T data) {
        if (data == null) {
            throw new IllegalArgumentException("data nesmu byt null");
        }
        if (!isInRegion(data, lowerBound, upperBound)) {
            throw new IllegalArgumentException("bod lezi mimo rozsahu stromu");
        }
        QuadNode<T> node = root;
        // root ma hlbku 0
        int depth = 0;
        while (true) {
            if (node.isLeaf()) {
                // prázdna oblasť (iba koreň na začiatku) alebo list s jedným záznamom
                if (node.getRecords().isEmpty() || hasSameKey(node.getRecords().getFirst(), data)
                        || depth >= maxDepth) {
                    node.getRecords().add(data);
                    return;
                }
                // oblasť je plná: rozdeľ ju a zopakuj pokus o vloženie
                split(node);
                continue;
            }
            int quadrant = getQuadrant(node, data);
            QuadNode<T> child = node.getChild(quadrant);
            if (child == null) {
                // prázdna oblasť: ulož záznam a nastav smerník na príslušnom indexe
                child = createChild(node, quadrant);
                child.getRecords().add(data);
                node.setChild(quadrant, child);
                return;
            }
            node = child;
            depth++;
        }
    }

    /**
     * Rozdelí oblasť na štyri kvadranty a presunie doterajšie
     * záznamy do príslušného kvadrantu.
     * O(r), kde r je počet presúvaných záznamov.
     */
    private void split(QuadNode<T> node) {
        node.createChildren();
        int quadrant = getQuadrant(node, node.getRecords().getFirst());
        QuadNode<T> child = createChild(node, quadrant);
        child.getRecords().addAll(node.getRecords());
        node.getRecords().clear();
        node.setChild(quadrant, child);
    }

    /** O(1) – majú dva záznamy rovnaké kľúče vo všetkých dimenziách? */
    private boolean hasSameKey(T a, T b) {
        for (int d = 0; d < DIMENSIONS; d++) {
            if (comparator.compare(a, b, d) != 0) {
                return false;
            }
        }
        return true;
    }

    /**
     * O(1) – určí kvadrant oblasti uzla, do ktorého patrí bod. Hodnoty rovné
     * stredu patria do východného, resp. severného kvadrantu.
     */
    private int getQuadrant(QuadNode<T> node, T data) {
        T middle = comparator.middle(node.getLowerBound(), node.getUpperBound());
        boolean east = comparator.compare(data, middle, 0) >= 0;
        boolean north = comparator.compare(data, middle, 1) >= 0;
        if (north) {
            return east ? QuadNode.NORTH_EAST : QuadNode.NORTH_WEST;
        }
        return east ? QuadNode.SOUTH_EAST : QuadNode.SOUTH_WEST;
    }

    /** O(1) – vytvorí prázdny uzol s oblasťou zadaného kvadrantu uzla. */
    private QuadNode<T> createChild(QuadNode<T> node, int quadrant) {
        T lower = node.getLowerBound();
        T upper = node.getUpperBound();
        T middle = comparator.middle(lower, upper);
        switch (quadrant) {
            case QuadNode.NORTH_WEST:
                return new QuadNode<>(comparator.combine(lower, middle), comparator.combine(middle, upper));
            case QuadNode.NORTH_EAST:
                return new QuadNode<>(middle, upper);
            case QuadNode.SOUTH_EAST:
                return new QuadNode<>(comparator.combine(middle, lower), comparator.combine(upper, middle));
            default:
                return new QuadNode<>(lower, middle);
        }
    }

    /**
     * Nájde všetky záznamy ležiace celé v zadanej oblasti S, t. j. pre každú
     * dimenziu d platí: lower[d] <= data[d] <= upper[d] (hranice vrátane).
     *
     * Postup: od koreňa sa postupne sprístupňujú iba oblasti stromu, ktoré sa s S
     * prekrývajú. Oblasti, kde S nezasahuje, sa neprehľadávajú. V každom
     * navštívenom uzle (vnútornom aj liste) sa prejde zoznam uložených záznamov
     * a každý sa skontroluje, či patrí do S.
     *
     * Časová zložitosť: O(v * (1 + r)), kde v je počet navštívených uzlov,
     * ktorých oblasti sa prekrývajú s S a r je priemerný počet záznamov v uzle;
     * najhoršie O(n), ak S pokrýva celý strom. Výška je najviac maxDepth.
     * Pamäťová zložitosť: O(h) pre explicitný zásobník (nie rekurzia; v každom
     * uzle max. 4 potomkovia, teda zásobník má najviac 3h + 1 prvkov) + O(m) pre
     * výsledok, kde m je počet nájdených záznamov.
     *
     * @param lower dolná hranica oblasti S (minimá v každej dimenzii)
     * @param upper horná hranica oblasti S (maximá v každej dimenzii)
     * @return zoznam všetkých záznamov z oblasti S
     */
    public List<T> find(T lower, T upper) {
        List<T> result = new LinkedList<>();
        if (lower == null || upper == null || !overlaps(root, lower, upper)) {
            return result;
        }
        // Explicitný zásobník uzlov na spracovanie.
        LinkedList<QuadNode<T>> stack = new LinkedList<>();
        stack.push(root);

        while (!stack.isEmpty()) {
            QuadNode<T> node = stack.pop();

            // skontroluj záznamy uložené v uzle (vo vnútornom uzle aj v liste)
            for (T record : node.getRecords()) {
                if (isInRegion(record, lower, upper)) {
                    result.add(record);
                }
            }

            // list nemá potomkov
            if (node.isLeaf()) {
                continue;
            }
            // pridaj na zásobník iba neprázdnych potomkov, ktorých oblasť sa prekrýva s S
            for (int quadrant = 0; quadrant < QuadNode.CHILD_COUNT; quadrant++) {
                QuadNode<T> child = node.getChild(quadrant);
                if (child != null && overlaps(child, lower, upper)) {
                    stack.push(child);
                }
            }
        }
        return result;
    }

    /**
     * O(1) – prekrýva sa oblasť uzla s oblasťou S (hranice vrátane) v oboch
     * dimenziách?
     */
    private boolean overlaps(QuadNode<T> node, T lower, T upper) {
        for (int d = 0; d < DIMENSIONS; d++) {
            if (comparator.compare(node.getLowerBound(), upper, d) > 0
                    || comparator.compare(node.getUpperBound(), lower, d) < 0) {
                return false;
            }
        }
        return true;
    }

    /** O(1) – leží záznam v oblasti S (hranice vrátane) v oboch dimenziách? */
    private boolean isInRegion(T data, T lower, T upper) {
        for (int d = 0; d < DIMENSIONS; d++) {
            if (comparator.compare(data, lower, d) < 0
                    || comparator.compare(data, upper, d) > 0) {
                return false;
            }
        }
        return true;
    }
}
