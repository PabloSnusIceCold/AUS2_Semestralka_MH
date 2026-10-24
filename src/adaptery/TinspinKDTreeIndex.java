package adaptery;

// Trieda TinspinKDTreeIndex.java bola vygenerovaná pomocou AI modelu Gemini 3.8 Flash

import model.GpsPoint;
import org.tinspin.index.kdtree.KDTree;
import org.tinspin.index.kdtree.KDIterator;
import java.util.ArrayList;
import java.util.List;

/**
 * Adapter pre externu kniznicu TinSpin KDTree.
 */
public class TinspinKDTreeIndex implements SpatialIndex {
    private final KDTree<GpsPoint> tree = KDTree.create(2);

    @Override
    public String name() {
        return "Tinspin KDTree";
    }

    @Override
    public void insert(GpsPoint p) {
        tree.insert(new double[]{p.getX(), p.getY()}, p);
    }

    @Override
    public List<GpsPoint> find(GpsPoint lower, GpsPoint upper) {
        double[] min = new double[]{lower.getX(), lower.getY()};
        double[] max = new double[]{upper.getX(), upper.getY()};
        KDIterator<GpsPoint> it = tree.query(min, max);
        List<GpsPoint> res = new ArrayList<>();
        while (it.hasNext()) {
            res.add(it.next().value());
        }
        return res;
    }
}

