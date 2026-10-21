import java.util.LinkedList;
import java.util.List;

/**
 * Univerzálny Quad strom (bez rekurzie) s operáciami Insert a Find, ktorý
 * záznamy vo vrcholoch uchováva v K-d strome (nie v zozname).
 * Štýlom zodpovedá QuadTree&lt;T&gt;: porovnávanie a delenie kľúčov
 * zabezpečuje {@link QuadKDComparator}.
 *
 * Koreň reprezentuje celú oblasť zadanú na začiatku (rozsah kľúčov).
 * Ak oblasť obsahuje viac ako 1 záznam, delí sa na 4 kvadranty, až kým
 * nie je v každej oblasti najviac 1 záznam, alebo kým nie je dosiahnutá
 * maximálna povolená výška stromu (maxDepth). Koreň má hĺbku 0.
 * Uzol, ktorý sa už nesmie deliť, drží viac záznamov, a tie sú v K-d strome,
 * takže sa v nich dá efektívne vyhľadávať aj intervalovo.
 *
 * @param <T> typ dát uložených v strome
 */
public class QuadKDTree<T> {
    private static final int DIMENSIONS = 2;
    private final QuadKDComparator<T> comparator;
    private final T lowerBound;
    private final T upperBound;
    private final int maxDepth;
    private final QuadKDNode<T> root;

    /**
     * @param comparator funkcia porovnávajúca kľúče (pre Quad strom aj K-d strom),
     *                   počítajúca ich rozdiel a delenie na polovicu
     * @param lowerBound dolná hranica rozsahu kľúčov (minimá v oboch dimenziách)
     * @param upperBound horná hranica rozsahu kľúčov (maximá v oboch dimenziách)
     * @param maxDepth   maximálna povolená výška stromu (hĺbka uzla, ktorý sa ešte
     *                   smie deliť, je menšia ako maxDepth); po jej dosiahnutí sa
     *                   oblasť už nedelí a záznam sa pridá do K-d stromu vo vrchole
     */
    public QuadKDTree(QuadKDComparator<T> comparator, T lowerBound, T upperBound, int maxDepth) {
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
        this.root = new QuadKDNode<>(lowerBound, upperBound, comparator);
    }

    /**
     * Vloží bod do stromu.
     *
     * Postup: od koreňa sa hľadá existujúca oblasť, do ktorej bod patrí.
     * - Ak je oblasť (referencia v poli) prázdna, vytvorí sa list so záznamom
     * a nastaví sa naň smerník na príslušnom indexe poľa.
     * - Ak je oblasť plná (list už obsahuje záznam s iným kľúčom), rozdelí sa na
     * 4 kvadranty, existujúce záznamy sa presunú do príslušného kvadrantu
     * a pokus o vloženie sa zopakuje.
     * - Ak sa narazí na záznam s rovnakým kľúčom (alebo je dosiahnutá maxDepth),
     * bod sa pridá do K-d stromu priamo v danom vrchole.
     *
     * Časová zložitosť: O(maxDepth + hk), kde hk je výška K-d stromu vo vrchole,
     * do ktorého sa vkladá (pri rovnakých kľúčoch sa K-d strom degeneruje
     * na reťaz, hk potom rastie s počtom duplicít); každé delenie je O(r log r)
     * pre r presúvaných záznamov s rovnakým kľúčom.
     * Pamäťová zložitosť: O(1) pomocná pamäť (iteratívne, bez rekurzie);
     * pri blízkych bodoch pribudne najviac O(maxDepth) nových uzlov.
     *
     * @throws IllegalArgumentException ak bod leží mimo rozsahu stromu
     */
    public void insert(T data) {
        if (data == null) {
            throw new IllegalArgumentException("data nesmu byt null");
        }
        if (!isInRegion(data, lowerBound, upperBound)) {
            throw new IllegalArgumentException("bod lezi mimo rozsahu stromu");
        }
        QuadKDNode<T> node = root;
        int depth = 0;
        while (true) {
            if (node.isLeaf()) {
                // prázdna oblasť (iba koreň na začiatku) alebo list s jedným kľúčom
                if (node.hasNoRecords() || hasSameKey(node.getFirstRecord(), data)
                        || depth >= maxDepth) {
                    node.addRecord(data);
                    return;
                }
                // oblasť je plná: rozdeľ ju a zopakuj pokus o vloženie
                split(node);
                continue;
            }
            int quadrant = getQuadrant(node, data);
            QuadKDNode<T> child = node.getChild(quadrant);
            if (child == null) {
                // prázdna oblasť: ulož záznam a nastav smerník na príslušnom indexe
                child = createChild(node, quadrant);
                child.addRecord(data);
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
     */
    private void split(QuadKDNode<T> node) {
        node.createChildren();
        int quadrant = getQuadrant(node, node.getFirstRecord());
        QuadKDNode<T> child = createChild(node, quadrant);
        node.moveRecordsTo(child);
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
    private int getQuadrant(QuadKDNode<T> node, T data) {
        T middle = comparator.middle(node.getLowerBound(), node.getUpperBound());
        boolean east = comparator.compare(data, middle, 0) >= 0;
        boolean north = comparator.compare(data, middle, 1) >= 0;
        if (north) {
            return east ? QuadKDNode.NORTH_EAST : QuadKDNode.NORTH_WEST;
        }
        return east ? QuadKDNode.SOUTH_EAST : QuadKDNode.SOUTH_WEST;
    }

    /** O(1) – vytvorí prázdny uzol s oblasťou zadaného kvadrantu uzla. */
    private QuadKDNode<T> createChild(QuadKDNode<T> node, int quadrant) {
        T lower = node.getLowerBound();
        T upper = node.getUpperBound();
        T middle = comparator.middle(lower, upper);
        switch (quadrant) {
            case QuadKDNode.NORTH_WEST:
                return new QuadKDNode<>(comparator.combine(lower, middle), comparator.combine(middle, upper),
                        comparator);
            case QuadKDNode.NORTH_EAST:
                return new QuadKDNode<>(middle, upper, comparator);
            case QuadKDNode.SOUTH_EAST:
                return new QuadKDNode<>(comparator.combine(middle, lower), comparator.combine(upper, middle),
                        comparator);
            default:
                return new QuadKDNode<>(lower, middle, comparator);
        }
    }

    /**
     * Nájde všetky záznamy ležiace celé v zadanej oblasti S, t. j. pre každú
     * dimenziu d platí: lower[d] <= data[d] <= upper[d] (hranice vrátane).
     *
     * Postup: od koreňa sa postupne sprístupňujú iba oblasti stromu, ktoré sa s S
     * prekrývajú. Oblasti, kde S nezasahuje, sa neprehľadávajú. V každom
     * navštívenom uzle, ktorý má záznamy, sa intervalovo prehľadá K-d strom
     * uzla (a nie celý zoznam záznamov).
     *
     * Časová zložitosť: O(v + sum(Fi)), kde v je počet navštívených uzlov,
     * ktorých oblasti sa prekrývajú s S, a Fi je cena intervalového
     * vyhľadávania v K-d strome i-teho navštíveného uzla (priemerne
     * O(sqrt(ri) + mi) pre ri záznamov a mi nájdených záznamov v uzle);
     * najhoršie O(n), ak S pokrýva celý strom. Výška Quad stromu je najviac
     * maxDepth.
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
        LinkedList<QuadKDNode<T>> stack = new LinkedList<>();
        stack.push(root);

        while (!stack.isEmpty()) {
            QuadKDNode<T> node = stack.pop();

            // intervalové vyhľadávanie v K-d strome záznamov uzla
            if (!node.hasNoRecords()) {
                result.addAll(node.findRecords(lower, upper));
            }

            // list nemá potomkov
            if (node.isLeaf()) {
                continue;
            }
            // pridaj na zásobník iba neprázdnych potomkov, ktorých oblasť sa prekrýva s S
            for (int quadrant = 0; quadrant < QuadKDNode.CHILD_COUNT; quadrant++) {
                QuadKDNode<T> child = node.getChild(quadrant);
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
    private boolean overlaps(QuadKDNode<T> node, T lower, T upper) {
        for (int d = 0; d < DIMENSIONS; d++) {
            if (comparator.compare(node.getLowerBound(), upper, d) > 0
                    || comparator.compare(node.getUpperBound(), lower, d) < 0) {
                return false;
            }
        }
        return true;
    }

    /** O(1) – leží záznam v oblasti (hranice vrátane) v oboch dimenziách? */
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
