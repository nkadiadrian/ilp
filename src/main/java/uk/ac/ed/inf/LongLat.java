package uk.ac.ed.inf;

public class LongLat {
    public static final double DISTANCE_TOLERANCE_IN_DEGREES = 0.00015;
    public static final double DRONE_MOVE_LENGTH = 0.00015;
    public static final int HOVERING_ANGLE = -999;

    private static final double MINIMUM_LONGITUDE = -3.192473;
    private static final double MAXIMUM_LONGITUDE = -3.184319;
    private static final double MINIMUM_LATITUDE = 55.942617;
    private static final double MAXIMUM_LATITUDE = 55.946233;

    double longitude;
    double latitude;

    public LongLat(double longitude, double latitude) {
        this.longitude = longitude;
        this.latitude = latitude;
    }

    public boolean isConfined () {
        return (longitude > MINIMUM_LONGITUDE &
                longitude < MAXIMUM_LONGITUDE &
                latitude > MINIMUM_LATITUDE &
                latitude < MAXIMUM_LATITUDE);
    }

    public double distanceTo(LongLat to) {
        return Math.sqrt(((this.longitude - to.longitude) * (this.longitude - to.longitude)) + ((this.latitude - to.latitude) * (this.latitude - to.latitude)));
    }

    public boolean closeTo(LongLat target) {
        return distanceTo(target) < DISTANCE_TOLERANCE_IN_DEGREES;
    }

    public LongLat nextPosition(int angle) {
        if (angle == HOVERING_ANGLE) {
            return new LongLat(this.longitude, this.latitude);
        } else {
            assert (angle < 360 & angle >= 0 & angle % 10 == 0);

            double angleInRadians = Math.toRadians(angle);
            return new LongLat(this.longitude + (DRONE_MOVE_LENGTH * Math.cos(angleInRadians)), this.latitude + (DRONE_MOVE_LENGTH * Math.sin(angleInRadians)));
        }
    }
}
