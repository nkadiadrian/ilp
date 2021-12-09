package uk.ac.ed.inf.drone.helpers;

import com.mapbox.geojson.Feature;
import com.mapbox.geojson.FeatureCollection;
import com.mapbox.geojson.Point;
import com.mapbox.geojson.Polygon;
import uk.ac.ed.inf.LongLat;

import java.awt.geom.Line2D;
import java.util.ArrayList;
import java.util.List;

public class BoundaryUtils {
    public static boolean moveIntersectsZone(LongLat newPosition, LongLat initialPosition, List<Line2D> boundaries) {
        Line2D moveLine = new Line2D.Double(initialPosition.getLongitude(), initialPosition.getLatitude(),
                newPosition.getLongitude(), newPosition.getLatitude());
        // Loop through all boundaries of No-Fly Zones and check if the move intersects.
        for (Line2D boundary : boundaries) {
            if (boundary.intersectsLine(moveLine)) {
                return true;
            }
        }
        return false;
    }
    /*
     * Return true if the line formed by the move goes into any No-Fly Zone, false if not.
     */

    public static ArrayList<Line2D> getNoFlyZoneBoundaries(FeatureCollection noFlyZone) {
        ArrayList<Line2D> noFlyBoundaries = new ArrayList<>();
        // Get all features from the map, and break them down into all boundary lines.
        // Add the boundary lines to noFlyBoundaries.
        List<List<Point>> pointLists = new ArrayList<>();
        assert noFlyZone.features() != null;
        for (Feature feature : noFlyZone.features()) {
            Polygon polygon = (Polygon) feature.geometry();
            assert polygon != null;
            List<Point> pointList = polygon.coordinates().get(0);
            pointLists.add(pointList);
            addPointsToBoundaries(pointList, noFlyBoundaries);
        }
        List<Point> aggregatedPointList = aggregatePointLists(pointLists);
        List<Point> convexHullPoints = ConvexHull.getConvexHull(aggregatedPointList);
        addPointsToBoundaries(convexHullPoints, noFlyBoundaries);
        return noFlyBoundaries;
    }

    private static void addPointsToBoundaries(List<Point> pointList, List<Line2D> boundaries) {
        for (int i = 0; i < pointList.size() - 1; i++) {
            Point pointFrom = pointList.get(i);
            Point pointTo = pointList.get(i + 1);
            Line2D line = new Line2D.Double(pointFrom.longitude(), pointFrom.latitude(), pointTo.longitude(), pointTo.latitude());
            boundaries.add(line);
        }
    }

    private static List<Point> aggregatePointLists(List<List<Point>> pointLists) {
        List<Point> aggregateList = ConvexHull.getConvexHull(pointLists.get(0));
        for (int i = 1; i < 5; i++) {
            int closestPointMainIndex = 0;
            int closestPointIndex = 0;
            double minDist = Double.POSITIVE_INFINITY;
            List<Point> pointList = ConvexHull.getConvexHull(pointLists.get(i));
            for (int j = 0; j < pointList.size() - 1; j++) {
                LongLat point = new LongLat(pointList.get(j));
                for (int k = 0; k < aggregateList.size(); k++) {
                    LongLat mainPoint = new LongLat(aggregateList.get(k));
                    double distance = point.distanceTo(mainPoint);
                    if (distance < minDist) {
                        minDist = distance;
                        closestPointIndex = j;
                        closestPointMainIndex = k;
                    }
                }
            }

            List<Point> newHull = pointList.subList(closestPointIndex, pointList.size() - 1);
            newHull.addAll(pointList.subList(0, closestPointIndex));
            aggregateList.addAll(closestPointMainIndex, newHull);
        }

        return aggregateList;
    }
}
