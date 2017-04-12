package micdm.transportlive2.data.loaders.remote;

import java.util.List;

public class GetPathResponse {

    public static class SegmentsGeoJson {

        public static class Feature {

            public static class Geometry {

                public final List<List<Float>> coordinates;

                Geometry(List<List<Float>> coordinates) {
                    this.coordinates = coordinates;
                }
            }

            public final Geometry geometry;

            Feature(Geometry geometry) {
                this.geometry = geometry;
            }
        }

        public final List<Feature> features;

        SegmentsGeoJson(List<Feature> features) {
            this.features = features;
        }
    }

    public static class StopPointsGeoJson {

        public static class Feature {

            public static class Geometry {

                public final List<Float> coordinates;

                public Geometry(List<Float> coordinates) {
                    this.coordinates = coordinates;
                }
            }

            public static class Properties {

                public final String mid;
                public final String name;

                public Properties(String mid, String name) {
                    this.mid = mid;
                    this.name = name;
                }
            }

            public final Geometry geometry;
            public final Properties properties;

            public Feature(Geometry geometry, Properties properties) {
                this.geometry = geometry;
                this.properties = properties;
            }
        }

        public final List<Feature> features;

        public StopPointsGeoJson(List<Feature> features) {
            this.features = features;
        }
    }

    public final SegmentsGeoJson SegmentsGeoJson;
    public final StopPointsGeoJson StopPointsGeoJson;

    GetPathResponse(GetPathResponse.SegmentsGeoJson segmentsGeoJson, GetPathResponse.StopPointsGeoJson stopPointsGeoJson) {
        SegmentsGeoJson = segmentsGeoJson;
        StopPointsGeoJson = stopPointsGeoJson;
    }
}
