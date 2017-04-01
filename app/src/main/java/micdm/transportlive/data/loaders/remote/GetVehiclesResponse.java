package micdm.transportlive.data.loaders.remote;

public class GetVehiclesResponse {

    public static class Auto {

        public final String AutoId;
        public final String PathwayId;

        Auto(String autoId, String pathwayId) {
            AutoId = autoId;
            PathwayId = pathwayId;
        }
    }

    public static class Point {

        public final float Lat;
        public final float Lon;
        public final float Dir;

        Point(float lat, float lon, float dir) {
            Lat = lat;
            Lon = lon;
            Dir = dir;
        }
    }

    public final Auto Auto;
    public final Point Point;

    GetVehiclesResponse(Auto auto, GetVehiclesResponse.Point point) {
        Auto = auto;
        Point = point;
    }
}
