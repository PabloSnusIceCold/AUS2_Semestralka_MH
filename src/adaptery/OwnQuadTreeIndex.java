package adaptery;

import model.GpsPoint;
import model.GpsComparator;
import stromy.QuadTree;
import java.util.List;

/** Adapter vlastneho Quad stromu na rozhranie SpatialIndex. */
public class OwnQuadTreeIndex implements SpatialIndex {
    private final QuadTree<GpsPoint> tree;

    public OwnQuadTreeIndex(double minX, double minY, double maxX, double maxY, int maxDepth) {
        tree = new QuadTree<>(new GpsComparator(), new GpsPoint(-1, minX, minY), new GpsPoint(-1, maxX, maxY),
                maxDepth);
    }

    @Override
    public String name() {
        return "Vlastny QuadTree";
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

