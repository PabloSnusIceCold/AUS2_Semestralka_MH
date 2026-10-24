package adaptery;

import model.GpsPoint;
import edu.wlu.cs.levy.CG.KDTree;
import edu.wlu.cs.levy.CG.KeyDuplicateException;
import edu.wlu.cs.levy.CG.KeySizeException;
import java.util.ArrayList;
import java.util.List;

/**
 * Adapter pre externu kniznicu Levy's KD-Tree.
 */
public class LevyKDTreeIndex implements SpatialIndex {
    private final KDTree<GpsPoint> tree = new KDTree<>(2);

    @Override
    public String name() {
        return "Levy KDTree";
    }

    @Override
    public void insert(GpsPoint p) {
        try {
            tree.insert(new double[]{p.getX(), p.getY()}, p);
        } catch (KeyDuplicateException e) {
            // Levy KDTree nepodporuje duplicitne suradnice kluca
        } catch (KeySizeException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<GpsPoint> find(GpsPoint lower, GpsPoint upper) {
        try {
            double[] low = new double[]{lower.getX(), lower.getY()};
            double[] upp = new double[]{upper.getX(), upper.getY()};
            List<GpsPoint> list = tree.range(low, upp);
            return list != null ? list : new ArrayList<>();
        } catch (KeySizeException e) {
            throw new RuntimeException(e);
        }
    }
}

