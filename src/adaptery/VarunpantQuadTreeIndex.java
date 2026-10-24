package adaptery;

// Trieda VarunpantQuadTreeIndex.java bola vygenerovaná pomocou AI modelu Gemini 3.8 Flash

import model.GpsPoint;
import com.github.varunpant.quadtree.Point;
import com.github.varunpant.quadtree.QuadTree;
import java.util.ArrayList;
import java.util.List;

/**
 * Adapter pre externu kniznicu Varunpant QuadTree.
 */
public class VarunpantQuadTreeIndex implements SpatialIndex {
    private final QuadTree<GpsPoint> tree;

    public VarunpantQuadTreeIndex(double minX, double minY, double maxX, double maxY) {
        // Mierne rozsirenie hranic aby body na presnom okraji nespadli do Out of bounds
        double padX = Math.max(1.0, (maxX - minX) * 0.001);
        double padY = Math.max(1.0, (maxY - minY) * 0.001);
        this.tree = new QuadTree<>(minX - padX, minY - padY, maxX + padX, maxY + padY);
    }

    @Override
    public String name() {
        return "Varunpant QuadTree";
    }

    @Override
    public void insert(GpsPoint p) {
        try {
            tree.set(p.getX(), p.getY(), p);
        } catch (Exception e) {
            // Ignorovanie pripadnych vynimiek mimo hranic
        }
    }

    @Override
    public List<GpsPoint> find(GpsPoint lower, GpsPoint upper) {
        Point<GpsPoint>[] arr = tree.searchIntersect(lower.getX(), lower.getY(), upper.getX(), upper.getY());
        List<GpsPoint> res = new ArrayList<>(arr != null ? arr.length : 0);
        if (arr != null) {
            for (Point<GpsPoint> pt : arr) {
                if (pt != null && pt.getValue() != null) {
                    res.add(pt.getValue());
                }
            }
        }
        return res;
    }
}

