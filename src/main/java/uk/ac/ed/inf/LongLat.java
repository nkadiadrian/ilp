package uk.ac.ed.inf;

public class LongLat {
    private static final double THRESHOLD = 0.00015;
    private static final double MINIMUM_LONGITUDE = 0;
    private static final double MAXIMUM_LONGITUDE = 0;
    private static final double MINUMUM_LATITUDE = 0;
    private static final double MAXIMUM_LATITUDE = 0;

    double longitude;
    double latitude;

    public LongLat(double longitude, double latitude) {
        this.longitude = longitude;
        this.latitude = latitude;
    }

    public boolean isConfined () {
        return (longitude > MINIMUM_LONGITUDE &
                longitude < MAXIMUM_LONGITUDE &
                latitude > MINUMUM_LATITUDE &
                latitude < MAXIMUM_LATITUDE);
    }

    public double distanceTo (LongLat to) {
        return Math.sqrt(((this.longitude - to.longitude) * (this.longitude - to.longitude)) - (this.latitude - to.latitude) * (this.latitude - to.latitude));
    }

    public boolean closeTo (LongLat target) {
        return distanceTo(target) < THRESHOLD;
    }

    public LongLat nextPosition (int angle) {
        if (angle == -999) {
            return new LongLat(this.longitude, this.latitude);
        } else {
            double angleInRadians = Math.toRadians(angle);
            return new LongLat(this.longitude + (THRESHOLD * Math.cos(angleInRadians)), this.latitude + (THRESHOLD * Math.sin(angleInRadians)));
        }
    }
}
