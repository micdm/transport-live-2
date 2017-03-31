package micdm.transportlive.data;

import java.util.List;

class GetPathResponse {

    static class SegmentsGeoJson {

        static class Feature {

            static class Geometry {

                final List<List<Float>> coordinates;

                Geometry(List<List<Float>> coordinates) {
                    this.coordinates = coordinates;
                }
            }

            final Geometry geometry;

            Feature(Geometry geometry) {
                this.geometry = geometry;
            }
        }

        final List<Feature> features;

        SegmentsGeoJson(List<Feature> features) {
            this.features = features;
        }
    }

    final SegmentsGeoJson SegmentsGeoJson;

    GetPathResponse(GetPathResponse.SegmentsGeoJson segmentsGeoJson) {
        SegmentsGeoJson = segmentsGeoJson;
    }
}
