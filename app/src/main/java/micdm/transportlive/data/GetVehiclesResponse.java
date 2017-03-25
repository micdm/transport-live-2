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

        final float Lat;
        final float Lon;
        final float Dir;

        Point(float lat, float lon, float dir) {
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
