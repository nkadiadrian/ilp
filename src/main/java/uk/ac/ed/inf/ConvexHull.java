package uk.ac.ed.inf;

import com.mapbox.geojson.Point;

import java.awt.*;
import java.awt.geom.Line2D;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
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

    private static boolean isInside(Point fromPoint, Point anglePoint, List<Point> hull) {
        Point2D fromAnglePoint = new Point2D.Double(fromPoint.longitude(), fromPoint.latitude());
        Point2D toAnglePoint = new Point2D.Double(anglePoint.longitude(), anglePoint.latitude());
        Line2D toAngleLine = new Line2D.Double(fromAnglePoint, toAnglePoint);

        for (int i = 0; i < hull.size() - 1;) {
            Point2D from = new Point2D.Double(hull.get(i).longitude(), hull.get(i).latitude());
            Point2D to = new Point2D.Double(hull.get(i + 1).longitude(), hull.get(i + 1).latitude());
            Line2D boundary = new Line2D.Double(from, to);
            if (!fromAnglePoint.equals(toAngleLine.getP1())) {
                if (!toAnglePoint.equals(toAngleLine.getP2())) {
                    if (boundary.intersectsLine(toAngleLine)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private static boolean counterClockwiseTurn(Point a, Point b, Point c) {
        return (b.longitude() - a.longitude()) * (c.latitude() - a.latitude()) > (b.latitude() - a.latitude()) * (c.longitude() - a.longitude());
    }
}

