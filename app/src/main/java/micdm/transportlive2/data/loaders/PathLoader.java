package micdm.transportlive2.data.loaders;

import java.util.List;

import io.reactivex.Single;
import micdm.transportlive2.data.loaders.remote.GetPathResponse;
import micdm.transportlive2.data.loaders.remote.ServerConnector;
import micdm.transportlive2.data.stores.PathStore;
import micdm.transportlive2.misc.Id;
import micdm.transportlive2.misc.IdFactory;
import micdm.transportlive2.models.ImmutablePath;
import micdm.transportlive2.models.ImmutablePoint;
import micdm.transportlive2.models.ImmutableStation;
import micdm.transportlive2.models.Path;

public class PathLoader extends BaseLoader<Path> {

    static class PathServerLoader implements ServerLoader<Path> {

        private final IdFactory idFactory;
        private final ServerConnector serverConnector;
        private final Id routeId;

        PathServerLoader(IdFactory idFactory, ServerConnector serverConnector, Id routeId) {
            this.idFactory = idFactory;
            this.serverConnector = serverConnector;
            this.routeId = routeId;
        }

        @Override
        public Single<Path> load() {
            return serverConnector.getPath(routeId).map(this::convert);
        }

        private Path convert(GetPathResponse response) {
            ImmutablePath.Builder builder = ImmutablePath.builder().routeId(routeId);
            for (GetPathResponse.SegmentsGeoJson.Feature feature: response.SegmentsGeoJson.features) {
                for (List<Double> values: feature.geometry.coordinates){
                    builder.addPoints(
                        ImmutablePoint.builder()
                            .latitude(values.get(1))
                            .longitude(values.get(0))
                            .build()
                    );
                }
            }
            for (GetPathResponse.StopPointsGeoJson.Feature feature: response.StopPointsGeoJson.features) {
                builder.addStations(
                    ImmutableStation.builder()
                        .id(idFactory.newInstance(feature.properties.mid))
                        .name(feature.properties.name)
                        .description("")
                        .location(
                            ImmutablePoint.builder()
                                .latitude(feature.geometry.coordinates.get(1))
                                .longitude(feature.geometry.coordinates.get(0))
                                .build()
                        )
                        .build()
                );
            }
            return builder.build();
        }
    }

    static class PathCacheClient extends DefaultCacheClient<Path> {

        PathCacheClient(PathStore store) {
            super(store);
        }
    }

    PathLoader(CacheClient<Path> cacheClient, ServerLoader<Path> serverLoader) {
        super(cacheClient, serverLoader);
    }
}
