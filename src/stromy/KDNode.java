package stromy;

// Trieda KDNode.java bola vygenerovanĂˇ pomocou AI (Claude Sonnet 5.5)

/**
 * Uzol K-D stromu (analĂłgia BstNode&lt;T&gt;). Obsahuje iba dĂˇta typu T
 * a referencie na ÄľavĂ©ho a pravĂ©ho syna.
 *
 * PamĂ¤ĹĄovĂˇ nĂˇroÄŤnosĹĄ: O(1) na uzol.
 *
 * @param <T> typ dĂˇt uloĹľenĂ˝ch v uzle
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

