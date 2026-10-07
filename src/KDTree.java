import java.util.LinkedList;
import java.util.List;

/**
 * Univerzálny K-D strom (bez rekurzie) s operáciami Insert a Find.
 * Štýlom zodpovedá BsTree&lt;T&gt;: uzly (KDNode&lt;T&gt;) obsahujú iba dáta
 * typu T,
 * porovnávanie zabezpečuje {@link KDComparator}.
 *
 * Konvencia: v uzle s hĺbkou h sa porovnáva dimenzia (h mod k).
 * - ľavý podstrom: dáta s hodnotou v dimenzii menšou alebo rovnou ako uzol
 * - pravý podstrom: dáta s hodnotou v dimenzii väčšou ako uzol
 * Pamäťová náročnosť stromu: O(n) – uzol drží dáta a 2 referencie;
 * rodič ani hĺbka sa neukladajú.
 *
 * @param <T> typ dát uložených v strome
 */
public class KDTree<T> {
    private final int dimensions;
    private final KDComparator<T> comparator;
    private KDNode<T> root;
    private int size;

    /**
     * @param dimensions počet dimenzií (k >= 1)
     * @param comparator funkcia porovnávajúca dáta v zadanej dimenzii
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
     * Vloží bod do stromu.
     *
     * Časová zložitosť: O(h), kde h je výška stromu.
     * - priemerne (náhodné poradie vkladania): O(log n)
     * - najhorší prípad (utriedený vstup, nevyvážený strom): O(n)
     * Pamäťová zložitosť: O(1) pomocná pamäť (iteratívny prechod, jeden nový uzol).
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
     * Nájde všetky záznamy ležiace celé v zadanej oblasti, t. j. pre každú
     * dimenziu d platí: lower[d] <= data[d] <= upper[d] (hranice vrátane).
     *
     * Časová zložitosť závisí od tvaru stromu a zadanej oblasti.
     * Pri vyváženom strome môže byť vyhľadávanie v niektorých prípadoch
     * horšie ako O(log n), najhorší prípad závisí od tvaru stromu.
     *
     * Pamäťová zložitosť: O(h) pre zásobník uzlov + O(m) pre výsledok,
     * kde h je výška stromu a m je počet nájdených záznamov.
     *
     * @param lower dolná hranica oblasti
     * @param upper horná hranica oblasti
     * @return zoznam nájdených dát
     */
    public List<T> find(T lower, T upper) {
        List<T> result = new LinkedList<>();
        if (root == null) {
            return result;
        }
        // Explicitný zásobník uzlov a im zodpovedajúcich dimenzií (LinkedList,
        // push/pop v O(1), bez nutnosti zväčšovania poľa).
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
            // Ľavý podstrom obsahuje hodnoty < uzol v dimenzii:
            // ide sa doň iba ak lower < uzol.
            boolean goLeft = node.getLeft() != null
                    && comparator.compare(lower, node.getData(), dimension) <= 0;
            // Pravý podstrom obsahuje hodnoty >= uzol:
            // ide sa doň iba ak upper >= uzol.
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

    /** O(k) – kontrola, či dáta ležia v oblasti vo všetkých dimenziách. */
    private boolean isInRegion(T data, T lower, T upper) {
        // prejdi všetky dimenzie
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
