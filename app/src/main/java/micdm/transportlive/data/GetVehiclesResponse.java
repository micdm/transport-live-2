package micdm.transportlive.data;

class GetVehiclesResponse {

    static class Auto {

        final String AutoId;
        final String PathwayId;

        Auto(String autoId, String pathwayId) {
            AutoId = autoId;
            PathwayId = pathwayId;
        }
    }

    static class Point {

        final double Lat;
        final double Lon;
        final double Dir;

        Point(double lat, double lon, double dir) {
            Lat = lat;
            Lon = lon;
            Dir = dir;
        }
    }

    final Auto Auto;
    final Point Point;

    GetVehiclesResponse(Auto auto, GetVehiclesResponse.Point point) {
        Auto = auto;
        Point = point;
    }
}
