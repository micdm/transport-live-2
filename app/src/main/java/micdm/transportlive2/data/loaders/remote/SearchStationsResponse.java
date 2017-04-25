package micdm.transportlive2.data.loaders.remote;

import java.util.Collection;

public class SearchStationsResponse {

    public static class PathwayStopPoint {

        public final String PathwayMilestoneId;
        public final String LegalName;
        public final String Description;
        public final double Latitude;
        public final double Longitude;

        public PathwayStopPoint(String pathwayMilestoneId, String legalName, String description, double latitude, double longitude) {
            PathwayMilestoneId = pathwayMilestoneId;
            LegalName = legalName;
            Description = description;
            Latitude = latitude;
            Longitude = longitude;
        }
    }

    public final Collection<PathwayStopPoint> PathwayStopPoints;

    SearchStationsResponse(Collection<PathwayStopPoint> pathwayStopPoints) {
        PathwayStopPoints = pathwayStopPoints;
    }
}
