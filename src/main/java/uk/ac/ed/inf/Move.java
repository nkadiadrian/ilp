package uk.ac.ed.inf;

import java.util.Objects;

public class Move {
    public String orderNo;
    public double fromLongitude;
    public double fromLatitude;
    public int angle;
    public double toLongitude;
    public double toLatitude;

    public Move(String orderNo, int angle, LongLat fromLocation, LongLat toLocation) {
        this.orderNo = orderNo;
        this.fromLongitude = fromLocation.getLongitude();
        this.fromLatitude = fromLocation.getLatitude();
        this.angle = angle;
        this.toLongitude = toLocation.getLongitude();
        this.toLatitude = toLocation.getLatitude();
    }

    public Move(Move move) {
        this.orderNo = move.orderNo;
        this.fromLongitude = move.fromLongitude;
        this.fromLatitude = move.fromLatitude;
        this.angle = move.angle;
        this.toLongitude = move.toLongitude;
        this.toLatitude = move.toLatitude;
    }

    public String getOrderNo() {
        return orderNo;
    }

    public double getFromLongitude() {
        return fromLongitude;
    }

    public double getFromLatitude() {
        return fromLatitude;
    }

    public int getAngle() {
        return angle;
    }

    public double getToLongitude() {
        return toLongitude;
    }

    public double getToLatitude() {
        return toLatitude;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Move move = (Move) o;
        return Double.compare(move.fromLongitude, fromLongitude) == 0 && Double.compare(move.fromLatitude, fromLatitude) == 0 && angle == move.angle && Double.compare(move.toLongitude, toLongitude) == 0 && Double.compare(move.toLatitude, toLatitude) == 0 && Objects.equals(orderNo, move.orderNo);
    }
}
