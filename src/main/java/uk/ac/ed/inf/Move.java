package uk.ac.ed.inf;

public class Move {
    private String orderNo;
    private double fromLongitude;
    private double fromLatitude;
    private int angle;
    private double toLongitude;
    private double toLatitude;

    public Move(String orderNo, double fromLongitude, double fromLatitude, int angle, double toLongitude, double toLatitude) {
        this.orderNo = orderNo;
        this.fromLongitude = fromLongitude;
        this.fromLatitude = fromLatitude;
        this.angle = angle;
        this.toLongitude = toLongitude;
        this.toLatitude = toLatitude;
    }
    public Move(String orderNo, int angle, LongLat fromLocation, LongLat toLocation) {
        this.orderNo = orderNo;
        this.fromLongitude = fromLocation.getLongitude();
        this.fromLatitude = fromLocation.getLatitude();
        this.angle = angle;
        this.toLongitude = toLocation.getLongitude();
        this.toLatitude = toLocation.getLatitude();
    }
}