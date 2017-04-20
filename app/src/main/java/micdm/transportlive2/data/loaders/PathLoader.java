package micdm.transportlive2.data.loaders;

import java.util.List;

import io.reactivex.Observable;
import io.reactivex.Single;
import micdm.transportlive2.data.loaders.remote.GetPathResponse;
import micdm.transportlive2.data.loaders.remote.ServerConnector;
import micdm.transportlive2.data.stores.BaseStore;
import micdm.transportlive2.data.stores.PathStore;
import micdm.transportlive2.misc.Id;
import micdm.transportlive2.misc.IdFactory;
import micdm.transportlive2.models.ImmutablePath;
import micdm.transportlive2.models.ImmutablePoint;
import micdm.transportlive2.models.ImmutableStation;
import micdm.transportlive2.models.Path;

public class PathLoader extends BaseLoader<PathLoader.Client, Path> {

    public interface Client {

        Observable<Id> getLoadPathRequests();
    }

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

    static class PathStoreClient extends DefaultStoreClient<PathStore.Client, Path> implements PathStore.Client {

        PathStoreClient(BaseStore<PathStore.Client, Path> store) {
            super(store);
        }

        @Override
        public void attach() {
            store.attach(this);
        }

        @Override
        public Observable<Path> getStorePathRequests() {
            return data;
        }
    }

    PathLoader(ClientHandler<Client> clientHandler, CacheLoader<Path> cacheLoader,
               ServerLoader<Path> serverLoader, StoreClient<Path> storeClient) {
        super(clientHandler, cacheLoader, serverLoader, storeClient);
    }
}
