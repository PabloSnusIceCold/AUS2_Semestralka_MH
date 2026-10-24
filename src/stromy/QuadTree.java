package stromy;

// Trieda QuadTree.java bola vygenerovanĂˇ pomocou AI (Claude Sonnet 5.5)

import java.util.LinkedList;
import java.util.List;

/**
 * UniverzĂˇlny Quad strom (bez rekurzie) s operĂˇciami Insert a Find.
 * Ĺ tĂ˝lom zodpovedĂˇ KDTree&lt;T&gt;: porovnĂˇvanie, rozdiel a delenie kÄľĂşÄŤov
 * zabezpeÄŤuje {@link QuadComparator}.
 *
 * KoreĹ reprezentuje celĂş oblasĹĄ zadanĂş na zaÄŤiatku (rozsah kÄľĂşÄŤov).
 * Ak oblasĹĄ obsahuje viac ako 1 zĂˇznam, delĂ­ sa na 4 kvadranty, aĹľ kĂ˝m
 * nie je v kaĹľdej oblasti najviac 1 zĂˇznam, alebo kĂ˝m nie je dosiahnutĂˇ
 * maximĂˇlna povolenĂˇ vĂ˝Ĺˇka stromu (maxDepth). KoreĹ mĂˇ hÄşbku 0.
 *
 * @param <T> typ dĂˇt uloĹľenĂ˝ch v strome
 */
public class QuadTree<T> {
    private static final int DIMENSIONS = 2;
    private final QuadComparator<T> comparator;
    private final T lowerBound;
    private final T upperBound;
    private final int maxDepth;
    private final QuadNode<T> root;

    /**
     * @param comparator funkcia porovnĂˇvajĂşca kÄľĂşÄŤe, poÄŤĂ­tajĂşca ich rozdiel
     *                   a delenie na polovicu
     * @param lowerBound dolnĂˇ hranica rozsahu kÄľĂşÄŤov (minimĂˇ v oboch dimenziĂˇch)
     * @param upperBound hornĂˇ hranica rozsahu kÄľĂşÄŤov (maximĂˇ v oboch dimenziĂˇch)
     * @param maxDepth   maximĂˇlna povolenĂˇ vĂ˝Ĺˇka stromu (hÄşbka uzla, ktorĂ˝ sa eĹˇte
     *                   smie deliĹĄ, je menĹˇia ako maxDepth); po jej dosiahnutĂ­ sa
     *                   oblasĹĄ uĹľ nedelĂ­ a zĂˇznam sa pridĂˇ do zoznamu vo vrchole
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
     * VloĹľĂ­ bod do stromu.
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
                // prĂˇzdna oblasĹĄ (iba koreĹ na zaÄŤiatku) alebo list s jednĂ˝m zĂˇznamom
                if (node.getRecords().isEmpty() || hasSameKey(node.getRecords().getFirst(), data)
                        || depth >= maxDepth) {
                    node.getRecords().add(data);
                    return;
                }
                // oblasĹĄ je plnĂˇ: rozdeÄľ ju a zopakuj pokus o vloĹľenie
                split(node);
                continue;
            }
            int quadrant = getQuadrant(node, data);
            QuadNode<T> child = node.getChild(quadrant);
            if (child == null) {
                // prĂˇzdna oblasĹĄ: uloĹľ zĂˇznam a nastav smernĂ­k na prĂ­sluĹˇnom indexe
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
     * RozdelĂ­ oblasĹĄ na Ĺˇtyri kvadranty a presunie doterajĹˇie
     * zĂˇznamy do prĂ­sluĹˇnĂ©ho kvadrantu.
     * O(r), kde r je poÄŤet presĂşvanĂ˝ch zĂˇznamov.
     */
    private void split(QuadNode<T> node) {
        node.createChildren();
        int quadrant = getQuadrant(node, node.getRecords().getFirst());
        QuadNode<T> child = createChild(node, quadrant);
        child.getRecords().addAll(node.getRecords());
        node.getRecords().clear();
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
    private int getQuadrant(QuadNode<T> node, T data) {
        T middle = comparator.middle(node.getLowerBound(), node.getUpperBound());
        boolean east = comparator.compare(data, middle, 0) >= 0;
        boolean north = comparator.compare(data, middle, 1) >= 0;
        if (north) {
            return east ? QuadNode.NORTH_EAST : QuadNode.NORTH_WEST;
        }
        return east ? QuadNode.SOUTH_EAST : QuadNode.SOUTH_WEST;
    }

    /** O(1) â€“ vytvorĂ­ prĂˇzdny uzol s oblasĹĄou zadanĂ©ho kvadrantu uzla. */
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
     * NĂˇjde vĹˇetky zĂˇznamy leĹľiace celĂ© v zadanej oblasti S, t. j. pre kaĹľdĂş
     * dimenziu d platĂ­: lower[d] <= data[d] <= upper[d] (hranice vrĂˇtane).
     *
     * Postup: od koreĹa sa postupne sprĂ­stupĹujĂş iba oblasti stromu, ktorĂ© sa s S
     * prekrĂ˝vajĂş. Oblasti, kde S nezasahuje, sa neprehÄľadĂˇvajĂş. V kaĹľdom
     * navĹˇtĂ­venom uzle (vnĂştornom aj liste) sa prejde zoznam uloĹľenĂ˝ch zĂˇznamov
     * a kaĹľdĂ˝ sa skontroluje, ÄŤi patrĂ­ do S.
     *
     * ÄŚasovĂˇ zloĹľitosĹĄ: O(v * (1 + r)), kde v je poÄŤet navĹˇtĂ­venĂ˝ch uzlov,
     * ktorĂ˝ch oblasti sa prekrĂ˝vajĂş s S a r je priemernĂ˝ poÄŤet zĂˇznamov v uzle;
     * najhorĹˇie O(n), ak S pokrĂ˝va celĂ˝ strom. VĂ˝Ĺˇka je najviac maxDepth.
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
        LinkedList<QuadNode<T>> stack = new LinkedList<>();
        stack.push(root);

        while (!stack.isEmpty()) {
            QuadNode<T> node = stack.pop();

            // skontroluj zĂˇznamy uloĹľenĂ© v uzle (vo vnĂştornom uzle aj v liste)
            for (T record : node.getRecords()) {
                if (isInRegion(record, lower, upper)) {
                    result.add(record);
                }
            }

            // list nemĂˇ potomkov
            if (node.isLeaf()) {
                continue;
            }
            // pridaj na zĂˇsobnĂ­k iba neprĂˇzdnych potomkov, ktorĂ˝ch oblasĹĄ sa prekrĂ˝va s S
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
     * O(1) â€“ prekrĂ˝va sa oblasĹĄ uzla s oblasĹĄou S (hranice vrĂˇtane) v oboch
     * dimenziĂˇch?
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

    /** O(1) â€“ leĹľĂ­ zĂˇznam v oblasti S (hranice vrĂˇtane) v oboch dimenziĂˇch? */
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

