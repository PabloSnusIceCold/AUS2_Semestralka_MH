package adaptery;

// Trieda GeoAssistKDTreeIndex.java bola vygenerovaná pomocou AI modelu Gemini 3.8 Flash

import model.GpsPoint;
import com.thegeekyasian.geoassist.kdtree.KDTree;
import com.thegeekyasian.geoassist.kdtree.KDTreeObject;
import com.thegeekyasian.geoassist.kdtree.geometry.BoundingBox;
import com.thegeekyasian.geoassist.kdtree.geometry.Point;
import java.util.ArrayList;
import java.util.List;

/**
 * Adapter pre externu kniznicu Geo Assist KDTree.
 */
public class GeoAssistKDTreeIndex implements SpatialIndex {
    private final KDTree<Long, GpsPoint> tree = new KDTree<>();
    private long idSeq = 0;

    @Override
    public String name() {
        return "Geo Assist KDTree";
    }

    @Override
    public void insert(GpsPoint p) {
        try {
            // Pouzitie unikatneho sekvencneho ID pre vnutornu mapu kniznice
            long objId = p.getId() >= 0 ? p.getId() : (++idSeq);
            KDTreeObject<Long, GpsPoint> obj = new KDTreeObject.Builder<Long, GpsPoint>()
                    .id(objId)
                    .latitude(p.getY())
                    .longitude(p.getX())
                    .data(p)
                    .build();
            tree.insert(obj);
        } catch (Exception e) {
            // Ignorovanie pripadnych duplicitnych suradnic, ktore kniznica nepodporuje
        }
    }

    @Override
    public List<GpsPoint> find(GpsPoint lower, GpsPoint upper) {
        Point lowerPt = new Point.Builder().latitude(lower.getY()).longitude(lower.getX()).build();
        Point upperPt = new Point.Builder().latitude(upper.getY()).longitude(upper.getX()).build();
        BoundingBox box = new BoundingBox.Builder().lowerPoint(lowerPt).upperPoint(upperPt).build();
        List<KDTreeObject<Long, GpsPoint>> raw = tree.findInRange(box);
        List<GpsPoint> res = new ArrayList<>(raw.size());
        for (KDTreeObject<Long, GpsPoint> item : raw) {
            res.add(item.getData());
        }
        return res;
    }
}

