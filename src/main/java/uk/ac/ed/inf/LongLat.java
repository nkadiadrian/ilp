package uk.ac.ed.inf;

import com.google.gson.annotations.SerializedName;
import com.mapbox.geojson.Point;

/**
 * Class to represent the location of the drone
 * Contains methods to help define the relative position and motion of the drone
 */
public class LongLat {
    /**
     * The tolerance and move length of the drone are specified in degrees
     * HOVERING_ANGLE pertains to the constant indicating that the drone is hovering
     */
    public static final double DISTANCE_TOLERANCE_IN_DEGREES = 0.00015;
    public static final double DRONE_MOVE_LENGTH = 0.00015;
    public static final int HOVERING_ANGLE = -999;

    public static final double APPLETON_TOWER_LONGITUDE = -3.186874;
    public static final double APPLETON_TOWER_LATITUDE = 55.944494;
    public static final LongLat APPLETON = new LongLat(LongLat.APPLETON_TOWER_LONGITUDE, LongLat.APPLETON_TOWER_LATITUDE);
    public static final int MAX_BEARING = 360;
    public static final int BEARING_MULTIPLE = 10;
    public static final int MIN_BEARING = 0;
    /**
     * MINIMUM_LONGITUDE, MAXIMUM_LONGITUDE, MINIMUM_LATITUDE and MAXIMUM_LATITUDE
     * define the confinement area for the drone, outside of which the drone is considered
     * to be malfunctioning
     */
    private static final double MINIMUM_LONGITUDE = -3.192473;
    private static final double MAXIMUM_LONGITUDE = -3.184319;
    private static final double MINIMUM_LATITUDE = 55.942617;
    private static final double MAXIMUM_LATITUDE = 55.946233;
    @SerializedName("lng")
    public double longitude;
    @SerializedName("lat")
    public double latitude;

    /**
     * @param longitude The longitude coordinate of the drone in degrees
     * @param latitude  The latitude coordinate of the drone in degrees
     */
    public LongLat(double longitude, double latitude) {
        this.longitude = longitude;
        this.latitude = latitude;
    }


    public LongLat(Point point) {
        this.longitude = point.longitude();
        this.latitude = point.latitude();
    }

    /**
     * Method to determine if the current object lies in the confinement region by determining if
     * the longitude and latitude are within their respective bounds
     *
     * @return true if the current value of the LongLat object places it in the confinement area and false otherwise
     */
    public boolean isConfined() {
        return (longitude > MINIMUM_LONGITUDE &
                longitude < MAXIMUM_LONGITUDE &
                latitude > MINIMUM_LATITUDE &
                latitude < MAXIMUM_LATITUDE);
    }

    /**
     * Calculates the euclidean distance between a point and the current point
     *
     * @param to a LongLat object at the coordinates of the point to which the distance from the current point is wanted
     * @return the euclidean distance in degrees between the current object's location and the to location.
     */
    public double distanceTo(LongLat to) {
        return Math.sqrt(((this.longitude - to.longitude) * (this.longitude - to.longitude)) + ((this.latitude - to.latitude) * (this.latitude - to.latitude)));
    }

    /**
     * Determines if the target point is close to the current point by the stipulated definition i.e. A point is close to
     * another point if the euclidean distance between them is less than 0.00015 degrees
     *
     * @param target a LongLat object at the coordinates of the point being compared to see if it close to the current location
     * @return true if the target point is close to the current point as defined by the tolerance and false otherwise.
     */
    public boolean closeTo(LongLat target) {
        return distanceTo(target) <= DISTANCE_TOLERANCE_IN_DEGREES;
    }

    /**
     * Calculates the next position from the current point based on the given movement angle.
     * If the angle is the specified hovering angle then the original position is returned.
     * <p>
     * However, for multiples of 10 from 0 to 350 the new position is calculated as follows:
     * - The bearing angle 0 is taken as east, 90 as north, 180 as west and 270 as south
     * - Since longitude increases in the eastward direction and latitude in the north, a triangle is formed
     * with a hypotenuse of one movement length, the longitude adjacent the angle and the latitude opposite the angle
     * - As such the increase in latitude can be calculated by the drone move length multiplied by the cosine of the angle
     * from cos (theta) = adjacent / hypotenuse
     * - Similarly, the increase in longitude can be calculate by the drone move length multiplied by the sine of the angle
     * from sin (theta) = opposite / hypotenuse
     *
     * @param angle The movement angle
     * @return A LongLat object at the new location of the drone were it to make one move at the specified angle
     */
    public LongLat nextPosition(int angle) {
        if (angle == HOVERING_ANGLE) {
            return new LongLat(this.longitude, this.latitude);
        } else {
            assert (angle < MAX_BEARING & angle >= MIN_BEARING & angle % BEARING_MULTIPLE == 0);

            double angleInRadians = Math.toRadians(angle);
            return new LongLat(this.longitude + (DRONE_MOVE_LENGTH * Math.cos(angleInRadians)), this.latitude + (DRONE_MOVE_LENGTH * Math.sin(angleInRadians)));
        }
    }

    public int getClosestAngleToDestination(LongLat destination) {
        double relativeNorth = destination.getLatitude() - this.latitude;
        double relativeEast = destination.getLongitude() - this.longitude;
        double angleRadians = Math.atan2(relativeNorth, relativeEast);
        double angleDegrees = Math.toDegrees(angleRadians);
        double bearing = (angleDegrees + MAX_BEARING) % MAX_BEARING;

        return (int) (BEARING_MULTIPLE * (Math.round(bearing / BEARING_MULTIPLE)));
    }

    public double getLongitude() {
        return longitude;
    }

    public double getLatitude() {
        return latitude;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LongLat longLat = (LongLat) o;
        return Double.compare(longLat.longitude, longitude) == 0 && Double.compare(longLat.latitude, latitude) == 0;
    }

    @Override
    public String toString() {
        return latitude + ", " + longitude;
    }
}
