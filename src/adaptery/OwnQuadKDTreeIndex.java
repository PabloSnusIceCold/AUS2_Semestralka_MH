package adaptery;

// Trieda OwnQuadKDTreeIndex.java bola vygenerovaná pomocou AI modelu Gemini 3.8 Flash

import model.GpsPoint;
import model.GpsComparator;
import stromy.QuadKDTree;
import java.util.List;

/** Adapter vlastneho Quad stromu s K-d stromom vo vrcholoch na rozhranie SpatialIndex. */
public class OwnQuadKDTreeIndex implements SpatialIndex {
    private final QuadKDTree<GpsPoint> tree;

    public OwnQuadKDTreeIndex(double minX, double minY, double maxX, double maxY, int maxDepth) {
        tree = new QuadKDTree<>(new GpsComparator(), new GpsPoint(-1, minX, minY), new GpsPoint(-1, maxX, maxY),
                maxDepth);
    }

    @Override
    public String name() {
        return "Vlastny QuadKDTree";
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

