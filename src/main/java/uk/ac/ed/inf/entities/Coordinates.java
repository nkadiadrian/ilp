package uk.ac.ed.inf.entities;

public class Coordinates { //TODO: FIX PARSING
    private double lng;
    private double lat;

    public Coordinates(double lng, double lat) {
        this.lng = lng;
        this.lat = lat;
    }

    public double getLng() {
        return lng;
    }

    public double getLat() {
        return lat;
    }
}
