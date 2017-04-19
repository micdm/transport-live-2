package micdm.transportlive2.data.loaders.remote;

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

        public final double Lat;
        public final double Lon;
        public final double Dir;

        Point(double lat, double lon, double dir) {
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
