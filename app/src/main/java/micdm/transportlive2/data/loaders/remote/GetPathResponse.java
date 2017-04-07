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

    public final SegmentsGeoJson SegmentsGeoJson;

    GetPathResponse(GetPathResponse.SegmentsGeoJson segmentsGeoJson) {
        SegmentsGeoJson = segmentsGeoJson;
    }
}
