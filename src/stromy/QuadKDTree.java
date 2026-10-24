package stromy;

import java.util.LinkedList;
import java.util.List;

/**
 * UniverzĂˇlny Quad strom (bez rekurzie) s operĂˇciami Insert a Find, ktorĂ˝
 * zĂˇznamy vo vrcholoch uchovĂˇva v K-d strome (nie v zozname).
 * Ĺ tĂ˝lom zodpovedĂˇ QuadTree&lt;T&gt;: porovnĂˇvanie a delenie kÄľĂşÄŤov
 * zabezpeÄŤuje {@link QuadKDComparator}.
 *
 * KoreĹ reprezentuje celĂş oblasĹĄ zadanĂş na zaÄŤiatku (rozsah kÄľĂşÄŤov).
 * Ak oblasĹĄ obsahuje viac ako 1 zĂˇznam, delĂ­ sa na 4 kvadranty, aĹľ kĂ˝m
 * nie je v kaĹľdej oblasti najviac 1 zĂˇznam, alebo kĂ˝m nie je dosiahnutĂˇ
 * maximĂˇlna povolenĂˇ vĂ˝Ĺˇka stromu (maxDepth). KoreĹ mĂˇ hÄşbku 0.
 * Uzol, ktorĂ˝ sa uĹľ nesmie deliĹĄ, drĹľĂ­ viac zĂˇznamov, a tie sĂş v K-d strome,
 * takĹľe sa v nich dĂˇ efektĂ­vne vyhÄľadĂˇvaĹĄ aj intervalovo.
 *
 * @param <T> typ dĂˇt uloĹľenĂ˝ch v strome
 */
public class QuadKDTree<T> {
    private static final int DIMENSIONS = 2;
    private final QuadKDComparator<T> comparator;
    private final T lowerBound;
    private final T upperBound;
    private final int maxDepth;
    private final QuadKDNode<T> root;

    /**
     * @param comparator funkcia porovnĂˇvajĂşca kÄľĂşÄŤe (pre Quad strom aj K-d strom),
     *                   poÄŤĂ­tajĂşca ich rozdiel a delenie na polovicu
     * @param lowerBound dolnĂˇ hranica rozsahu kÄľĂşÄŤov (minimĂˇ v oboch dimenziĂˇch)
     * @param upperBound hornĂˇ hranica rozsahu kÄľĂşÄŤov (maximĂˇ v oboch dimenziĂˇch)
     * @param maxDepth   maximĂˇlna povolenĂˇ vĂ˝Ĺˇka stromu (hÄşbka uzla, ktorĂ˝ sa eĹˇte
     *                   smie deliĹĄ, je menĹˇia ako maxDepth); po jej dosiahnutĂ­ sa
     *                   oblasĹĄ uĹľ nedelĂ­ a zĂˇznam sa pridĂˇ do K-d stromu vo vrchole
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
     * VloĹľĂ­ bod do stromu.
     *
     * Postup: od koreĹa sa hÄľadĂˇ existujĂşca oblasĹĄ, do ktorej bod patrĂ­.
     * - Ak je oblasĹĄ (referencia v poli) prĂˇzdna, vytvorĂ­ sa list so zĂˇznamom
     * a nastavĂ­ sa naĹ smernĂ­k na prĂ­sluĹˇnom indexe poÄľa.
     * - Ak je oblasĹĄ plnĂˇ (list uĹľ obsahuje zĂˇznam s inĂ˝m kÄľĂşÄŤom), rozdelĂ­ sa na
     * 4 kvadranty, existujĂşce zĂˇznamy sa presunĂş do prĂ­sluĹˇnĂ©ho kvadrantu
     * a pokus o vloĹľenie sa zopakuje.
     * - Ak sa narazĂ­ na zĂˇznam s rovnakĂ˝m kÄľĂşÄŤom (alebo je dosiahnutĂˇ maxDepth),
     * bod sa pridĂˇ do K-d stromu priamo v danom vrchole.
     *
     * ÄŚasovĂˇ zloĹľitosĹĄ: O(maxDepth + hk), kde hk je vĂ˝Ĺˇka K-d stromu vo vrchole,
     * do ktorĂ©ho sa vkladĂˇ (pri rovnakĂ˝ch kÄľĂşÄŤoch sa K-d strom degeneruje
     * na reĹĄaz, hk potom rastie s poÄŤtom duplicĂ­t); kaĹľdĂ© delenie je O(r log r)
     * pre r presĂşvanĂ˝ch zĂˇznamov s rovnakĂ˝m kÄľĂşÄŤom.
     * PamĂ¤ĹĄovĂˇ zloĹľitosĹĄ: O(1) pomocnĂˇ pamĂ¤ĹĄ (iteratĂ­vne, bez rekurzie);
     * pri blĂ­zkych bodoch pribudne najviac O(maxDepth) novĂ˝ch uzlov.
     *
     * @throws IllegalArgumentException ak bod leĹľĂ­ mimo rozsahu stromu
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
                // prĂˇzdna oblasĹĄ (iba koreĹ na zaÄŤiatku) alebo list s jednĂ˝m kÄľĂşÄŤom
                if (node.hasNoRecords() || hasSameKey(node.getFirstRecord(), data)
                        || depth >= maxDepth) {
                    node.addRecord(data);
                    return;
                }
                // oblasĹĄ je plnĂˇ: rozdeÄľ ju a zopakuj pokus o vloĹľenie
                split(node);
                continue;
            }
            int quadrant = getQuadrant(node, data);
            QuadKDNode<T> child = node.getChild(quadrant);
            if (child == null) {
                // prĂˇzdna oblasĹĄ: uloĹľ zĂˇznam a nastav smernĂ­k na prĂ­sluĹˇnom indexe
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
     * RozdelĂ­ oblasĹĄ na Ĺˇtyri kvadranty a presunie doterajĹˇie
     * zĂˇznamy do prĂ­sluĹˇnĂ©ho kvadrantu.
     */
    private void split(QuadKDNode<T> node) {
        node.createChildren();
        int quadrant = getQuadrant(node, node.getFirstRecord());
        QuadKDNode<T> child = createChild(node, quadrant);
        node.moveRecordsTo(child);
        node.setChild(quadrant, child);
    }

    /** O(1) â€“ majĂş dva zĂˇznamy rovnakĂ© kÄľĂşÄŤe vo vĹˇetkĂ˝ch dimenziĂˇch? */
    private boolean hasSameKey(T a, T b) {
        for (int d = 0; d < DIMENSIONS; d++) {
            if (comparator.compare(a, b, d) != 0) {
                return false;
            }
        }
        return true;
    }

    /**
     * O(1) â€“ urÄŤĂ­ kvadrant oblasti uzla, do ktorĂ©ho patrĂ­ bod. Hodnoty rovnĂ©
     * stredu patria do vĂ˝chodnĂ©ho, resp. severnĂ©ho kvadrantu.
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

    /** O(1) â€“ vytvorĂ­ prĂˇzdny uzol s oblasĹĄou zadanĂ©ho kvadrantu uzla. */
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
     * NĂˇjde vĹˇetky zĂˇznamy leĹľiace celĂ© v zadanej oblasti S, t. j. pre kaĹľdĂş
     * dimenziu d platĂ­: lower[d] <= data[d] <= upper[d] (hranice vrĂˇtane).
     *
     * Postup: od koreĹa sa postupne sprĂ­stupĹujĂş iba oblasti stromu, ktorĂ© sa s S
     * prekrĂ˝vajĂş. Oblasti, kde S nezasahuje, sa neprehÄľadĂˇvajĂş. V kaĹľdom
     * navĹˇtĂ­venom uzle, ktorĂ˝ mĂˇ zĂˇznamy, sa intervalovo prehÄľadĂˇ K-d strom
     * uzla (a nie celĂ˝ zoznam zĂˇznamov).
     *
     * ÄŚasovĂˇ zloĹľitosĹĄ: O(v + sum(Fi)), kde v je poÄŤet navĹˇtĂ­venĂ˝ch uzlov,
     * ktorĂ˝ch oblasti sa prekrĂ˝vajĂş s S, a Fi je cena intervalovĂ©ho
     * vyhÄľadĂˇvania v K-d strome i-teho navĹˇtĂ­venĂ©ho uzla (priemerne
     * O(sqrt(ri) + mi) pre ri zĂˇznamov a mi nĂˇjdenĂ˝ch zĂˇznamov v uzle);
     * najhorĹˇie O(n), ak S pokrĂ˝va celĂ˝ strom. VĂ˝Ĺˇka Quad stromu je najviac
     * maxDepth.
     * PamĂ¤ĹĄovĂˇ zloĹľitosĹĄ: O(h) pre explicitnĂ˝ zĂˇsobnĂ­k (nie rekurzia; v kaĹľdom
     * uzle max. 4 potomkovia, teda zĂˇsobnĂ­k mĂˇ najviac 3h + 1 prvkov) + O(m) pre
     * vĂ˝sledok, kde m je poÄŤet nĂˇjdenĂ˝ch zĂˇznamov.
     *
     * @param lower dolnĂˇ hranica oblasti S (minimĂˇ v kaĹľdej dimenzii)
     * @param upper hornĂˇ hranica oblasti S (maximĂˇ v kaĹľdej dimenzii)
     * @return zoznam vĹˇetkĂ˝ch zĂˇznamov z oblasti S
     */
    public List<T> find(T lower, T upper) {
        List<T> result = new LinkedList<>();
        if (lower == null || upper == null || !overlaps(root, lower, upper)) {
            return result;
        }
        // ExplicitnĂ˝ zĂˇsobnĂ­k uzlov na spracovanie.
        LinkedList<QuadKDNode<T>> stack = new LinkedList<>();
        stack.push(root);

        while (!stack.isEmpty()) {
            QuadKDNode<T> node = stack.pop();

            // intervalovĂ© vyhÄľadĂˇvanie v K-d strome zĂˇznamov uzla
            if (!node.hasNoRecords()) {
                result.addAll(node.findRecords(lower, upper));
            }

            // list nemĂˇ potomkov
            if (node.isLeaf()) {
                continue;
            }
            // pridaj na zĂˇsobnĂ­k iba neprĂˇzdnych potomkov, ktorĂ˝ch oblasĹĄ sa prekrĂ˝va s S
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
     * O(1) â€“ prekrĂ˝va sa oblasĹĄ uzla s oblasĹĄou S (hranice vrĂˇtane) v oboch
     * dimenziĂˇch?
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

    /** O(1) â€“ leĹľĂ­ zĂˇznam v oblasti (hranice vrĂˇtane) v oboch dimenziĂˇch? */
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

