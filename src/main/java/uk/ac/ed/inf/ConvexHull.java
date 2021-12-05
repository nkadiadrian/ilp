package uk.ac.ed.inf;

import com.mapbox.geojson.Point;

import java.util.ArrayList;
import java.util.List;

public class ConvexHull {
    public static List<Point> getConvexHull(List<Point> pointList) {
        List<Point> convexHull = new ArrayList<>(pointList);
        convexHull.remove(convexHull.size() - 1);

        int oldHullSize = 0;
        while (oldHullSize != convexHull.size()) {
            int fromPointIndex = 0;
            List<Point> newHull = new ArrayList<>();
            for (int i = 1; i < convexHull.size() + 1; i++) {
                int anglePointIndex = i % convexHull.size();
                int toPointIndex = (i + 1) % convexHull.size();

                Point fromPoint = convexHull.get(fromPointIndex);
                Point anglePoint = convexHull.get(anglePointIndex);
                Point toPoint = convexHull.get(toPointIndex);
                if (counterClockwiseTurn(fromPoint, anglePoint, toPoint)) {
                    newHull.add(anglePoint);
                    fromPointIndex++;
                }
            }
            oldHullSize = convexHull.size();
            convexHull = newHull;
        }

        convexHull.add(convexHull.get(0));
        return convexHull;
    }

    private static boolean counterClockwiseTurn(Point a, Point b, Point c) {
        return (b.longitude() - a.longitude()) * (c.latitude() - a.latitude()) > (b.latitude() - a.latitude()) * (c.longitude() - a.longitude());
    }
}

