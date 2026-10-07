/**
 * Uzol K-D stromu (analógia BstNode&lt;T&gt;). Obsahuje iba dáta typu T
 * a referencie na ľavého a pravého syna.
 *
 * Pamäťová náročnosť: O(1) na uzol.
 *
 * @param <T> typ dát uložených v uzle
 */
class KDNode<T> {
    private final T data;
    private KDNode<T> left;
    private KDNode<T> right;

    KDNode(T data) {
        this.data = data;
    }

    T getData() {
        return data;
    }

    KDNode<T> getLeft() {
        return left;
    }

    void setLeft(KDNode<T> left) {
        this.left = left;
    }

    KDNode<T> getRight() {
        return right;
    }

    void setRight(KDNode<T> right) {
        this.right = right;
    }
}
