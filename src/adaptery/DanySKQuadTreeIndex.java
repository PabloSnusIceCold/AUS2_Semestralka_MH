package adaptery;

// Trieda DanySKQuadTreeIndex.java bola vygenerovaná pomocou AI modelu Gemini 3.8 Flash

import model.GpsPoint;
import org.danilopianini.util.FlexibleQuadTree;
import java.util.ArrayList;
import java.util.List;

/**
 * Adapter pre externu kniznicu DanySK FlexibleQuadTree.
 */
public class DanySKQuadTreeIndex implements SpatialIndex {
    private final FlexibleQuadTree<GpsPoint> tree = new FlexibleQuadTree<>();

    @Override
    public String name() {
        return "DanySK FlexibleQuadTree";
    }

    @Override
    public void insert(GpsPoint p) {
        tree.insert(p, p.getX(), p.getY());
    }

    @Override
    public List<GpsPoint> find(GpsPoint lower, GpsPoint upper) {
        // FlexibleQuadTree.isIn pouziva < pre horne hranice (x < fx && y < fy).
        // Aby boli horne hranice vratane, dotazujeme az po Math.nextUp(...)
        List<GpsPoint> list = tree.query(lower.getX(), lower.getY(), Math.nextUp(upper.getX()), Math.nextUp(upper.getY()));
        return list != null ? list : new ArrayList<>();
    }
}

