package uk.ac.ed.inf.entities.web;

import com.google.gson.annotations.SerializedName;
import uk.ac.ed.inf.LongLat;

public class ThreeWordsDetails {
    @SerializedName("coordinates")
    private LongLat longLat;

    public ThreeWordsDetails(LongLat longLat) {
        this.longLat = longLat;
    }

    public LongLat getLongLat() {
        return longLat;
    }
}
