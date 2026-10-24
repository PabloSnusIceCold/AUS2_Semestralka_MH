import java.util.List;

/** Adapter vlastneho K-d stromu na rozhranie SpatialIndex. */
public class OwnKDTreeIndex implements SpatialIndex {
    private final KDTree<GpsPoint> tree = new KDTree<>(2, new GpsComparator());

    @Override
    public String name() {
        return "Vlastny KDTree";
    }

    @Override
    public void insert(GpsPoint p) {
        tree.insert(p);
    }

    @Override
    public List<GpsPoint> find(GpsPoint lower, GpsPoint upper) {
        return tree.find(lower, upper);
    }

    @Override
    public long comparisons() {
        return GpsComparator.calls;
    }
}
