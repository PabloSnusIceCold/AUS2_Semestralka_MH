package stromy;

// Trieda KDTree.java bola vygenerovanĂˇ pomocou AI (Claude Sonnet 5.5)

import java.util.LinkedList;
import java.util.List;

/**
 * UniverzĂˇlny K-D strom (bez rekurzie) s operĂˇciami Insert a Find.
 * Ĺ tĂ˝lom zodpovedĂˇ BsTree&lt;T&gt;: uzly (KDNode&lt;T&gt;) obsahujĂş iba dĂˇta
 * typu T,
 * porovnĂˇvanie zabezpeÄŤuje {@link KDComparator}.
 *
 * Konvencia: v uzle s hÄşbkou h sa porovnĂˇva dimenzia (h mod k).
 * - ÄľavĂ˝ podstrom: dĂˇta s hodnotou v dimenzii menĹˇou alebo rovnou ako uzol
 * - pravĂ˝ podstrom: dĂˇta s hodnotou v dimenzii vĂ¤ÄŤĹˇou ako uzol
 * PamĂ¤ĹĄovĂˇ nĂˇroÄŤnosĹĄ stromu: O(n) â€“ uzol drĹľĂ­ dĂˇta a 2 referencie;
 * rodiÄŤ ani hÄşbka sa neukladajĂş.
 *
 * @param <T> typ dĂˇt uloĹľenĂ˝ch v strome
 */
public class KDTree<T> {
    private final int dimensions;
    private final KDComparator<T> comparator;
    private KDNode<T> root;
    private int size;

    /**
     * @param dimensions poÄŤet dimenziĂ­ (k >= 1)
     * @param comparator funkcia porovnĂˇvajĂşca dĂˇta v zadanej dimenzii
     */
    public KDTree(int dimensions, KDComparator<T> comparator) {
        if (dimensions < 1) {
            throw new IllegalArgumentException("dimensions musi byt >= 1");
        }
        if (comparator == null) {
            throw new IllegalArgumentException("comparator nesmie byt null");
        }
        this.dimensions = dimensions;
        this.comparator = comparator;
    }

    public int size() {
        return size;
    }

    public boolean isEmpty() {
        return size == 0;
    }

    /**
     * VloĹľĂ­ bod do stromu.
     *
     * ÄŚasovĂˇ zloĹľitosĹĄ: O(h), kde h je vĂ˝Ĺˇka stromu.
     * - priemerne (nĂˇhodnĂ© poradie vkladania): O(log n)
     * - najhorĹˇĂ­ prĂ­pad (utriedenĂ˝ vstup, nevyvĂˇĹľenĂ˝ strom): O(n)
     * PamĂ¤ĹĄovĂˇ zloĹľitosĹĄ: O(1) pomocnĂˇ pamĂ¤ĹĄ (iteratĂ­vny prechod, jeden novĂ˝ uzol).
     */
    public void insert(T data) {
        if (data == null) {
            throw new IllegalArgumentException("data nesmu byt null");
        }
        KDNode<T> newNode = new KDNode<>(data);
        size++;
        if (root == null) {
            root = newNode;
            return;
        }
        KDNode<T> current = root;
        int dimension = 0;
        while (true) {
            // napr porovnaj 20 s 23, vysledok je -3 a to je < 0 takze true ide do lava
            // ak je vysledok 0 tak duplicity idu do lava
            if (comparator.compare(data, current.getData(), dimension) <= 0) {
                if (current.getLeft() == null) {
                    current.setLeft(newNode);
                    return;
                }
                current = current.getLeft();
            }
            // inak ide do prava
            else {
                if (current.getRight() == null) {
                    current.setRight(newNode);
                    return;
                }
                current = current.getRight();
            }
            dimension = (dimension + 1) % dimensions;
        }
    }

    /**
     * NĂˇjde vĹˇetky zĂˇznamy leĹľiace celĂ© v zadanej oblasti, t. j. pre kaĹľdĂş
     * dimenziu d platĂ­: lower[d] <= data[d] <= upper[d] (hranice vrĂˇtane).
     *
     * ÄŚasovĂˇ zloĹľitosĹĄ zĂˇvisĂ­ od tvaru stromu a zadanej oblasti.
     * Pri vyvĂˇĹľenom strome mĂ´Ĺľe byĹĄ vyhÄľadĂˇvanie v niektorĂ˝ch prĂ­padoch
     * horĹˇie ako O(log n), najhorĹˇĂ­ prĂ­pad zĂˇvisĂ­ od tvaru stromu.
     *
     * PamĂ¤ĹĄovĂˇ zloĹľitosĹĄ: O(h) pre zĂˇsobnĂ­k uzlov + O(m) pre vĂ˝sledok,
     * kde h je vĂ˝Ĺˇka stromu a m je poÄŤet nĂˇjdenĂ˝ch zĂˇznamov.
     *
     * @param lower dolnĂˇ hranica oblasti
     * @param upper hornĂˇ hranica oblasti
     * @return zoznam nĂˇjdenĂ˝ch dĂˇt
     */
    public List<T> find(T lower, T upper) {
        List<T> result = new LinkedList<>();
        if (root == null) {
            return result;
        }
        // ExplicitnĂ˝ zĂˇsobnĂ­k uzlov a im zodpovedajĂşcich dimenziĂ­ (LinkedList,
        // push/pop v O(1), bez nutnosti zvĂ¤ÄŤĹˇovania poÄľa).
        LinkedList<KDNode<T>> nodeStack = new LinkedList<>();
        LinkedList<Integer> dimensionStack = new LinkedList<>();
        nodeStack.push(root);
        dimensionStack.push(0);

        // cyklus bezi ak su este niake uzly na spracovanie
        while (!nodeStack.isEmpty()) {
            KDNode<T> node = nodeStack.pop();
            int dimension = dimensionStack.pop();

            // zisti ci je aktualny uzol v oblasti
            if (isInRegion(node.getData(), lower, upper)) {
                result.add(node.getData());
            }

            // ak je aktualna dimenzia 0 a celkova je 2 tak potom, 0(X) +1 % 2 = 1,
            // dalsia dimenzia ktoru budeme prehladavat bude 1(Y)
            int nextDimension = (dimension + 1) % dimensions;
            // Ä˝avĂ˝ podstrom obsahuje hodnoty <= uzol v dimenzii:
            // ide sa doĹ iba ak lower <= uzol.
            boolean goLeft = node.getLeft() != null
                    && comparator.compare(lower, node.getData(), dimension) <= 0;
            // PravĂ˝ podstrom obsahuje hodnoty > uzol:
            // ide sa doĹ iba ak upper > uzol.
            boolean goRight = node.getRight() != null
                    && comparator.compare(upper, node.getData(), dimension) > 0;

            // pridaj pravy uzol do stacku
            if (goRight) {
                nodeStack.push(node.getRight());
                dimensionStack.push(nextDimension);
            }
            // pridaj lavy uzol do stacku
            if (goLeft) {
                nodeStack.push(node.getLeft());
                dimensionStack.push(nextDimension);
            }
        }
        return result;
    }

    /** O(k) â€“ kontrola, ÄŤi dĂˇta leĹľia v oblasti vo vĹˇetkĂ˝ch dimenziĂˇch. */
    private boolean isInRegion(T data, T lower, T upper) {
        // prejdi vĹˇetky dimenzie
        for (int d = 0; d < dimensions; d++) {
            // porovnaj data so spodnou hranicou
            if (comparator.compare(data, lower, d) < 0
                    // porovnaj data s hornou hranicou
                    || comparator.compare(data, upper, d) > 0) {
                return false;
            }
        }
        return true;
    }
}

