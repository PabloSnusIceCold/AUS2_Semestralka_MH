package adaptery;

import model.GpsPoint;
import tree.QuadTree;
import java.util.ArrayList;
import java.util.List;

/**
 * Adapter pre externu kniznicu QuadTree v baliku tree (Kirsty Williams).
 */
public class TreeQuadTreeIndex implements SpatialIndex {
    private final QuadTree<GpsPoint> tree;

    public TreeQuadTreeIndex(double minX, double minY, double maxX, double maxY) {
        double padX = Math.max(1.0, (maxX - minX) * 0.001);
        double padY = Math.max(1.0, (maxY - minY) * 0.001);
        this.tree = new QuadTree<>(minX - padX, minY - padY, maxX + padX, maxY + padY);
    }

    @Override
    public String name() {
        return "Tree (Kirsty) QuadTree";
    }

    @Override
    public void insert(GpsPoint p) {
        tree.put(p.getX(), p.getY(), p);
    }

    @Override
    public List<GpsPoint> find(GpsPoint lower, GpsPoint upper) {
        // Box.contains pouziva ostru nerovnost < pre horne hranice (x < maxX && y < maxY).
        // Aby boli horne hranice vratane, dotazujeme az po Math.nextUp(...)
        return tree.get(lower.getX(), lower.getY(), Math.nextUp(upper.getX()), Math.nextUp(upper.getY()), new ArrayList<>());
    }
}

