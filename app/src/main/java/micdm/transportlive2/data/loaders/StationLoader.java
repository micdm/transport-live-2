package micdm.transportlive2.data.loaders;

import io.reactivex.Observable;
import io.reactivex.Single;
import micdm.transportlive2.data.loaders.remote.GetStationResponse;
import micdm.transportlive2.data.loaders.remote.ServerConnector;
import micdm.transportlive2.data.stores.BaseStore;
import micdm.transportlive2.data.stores.StationStore;
import micdm.transportlive2.misc.Id;
import micdm.transportlive2.misc.IdFactory;
import micdm.transportlive2.models.ImmutablePoint;
import micdm.transportlive2.models.ImmutableStation;
import micdm.transportlive2.models.Station;

public class StationLoader extends BaseLoader<StationLoader.Client, Station> {

    public interface Client {

        Observable<Object> getLoadStationRequests();
    }

    static class StationServerLoader implements ServerLoader<Station> {

        private final IdFactory idFactory;
        private final ServerConnector serverConnector;
        private final Id stationId;

        StationServerLoader(IdFactory idFactory, ServerConnector serverConnector, Id stationId) {
            this.idFactory = idFactory;
            this.serverConnector = serverConnector;
            this.stationId = stationId;
        }

        @Override
        public Single<Station> load() {
            return serverConnector.getStation(stationId).map(this::convert);
        }

        private Station convert(GetStationResponse response) {
            return ImmutableStation.builder()
                .id(idFactory.newInstance(response.PathwayMilestoneId))
                .name(response.LegalName)
                .description(response.Description == null ? "" : response.Description)
                .location(
                    ImmutablePoint.builder()
                        .latitude(response.Latitude)
                        .longitude(response.Longitude)
                        .build()
                )
                .build();
        }
    }

    static class StationStoreClient extends DefaultStoreClient<StationStore.Client, Station> implements StationStore.Client {

        StationStoreClient(BaseStore<StationStore.Client, Station> store) {
            super(store);
        }

        @Override
        public void attach() {
            store.attach(this);
        }

        @Override
        public Observable<Station> getStoreStationRequests() {
            return data;
        }
    }

    StationLoader(ClientHandler<Client> clientHandler, CacheLoader<Station> cacheLoader,
                  ServerLoader<Station> serverLoader, StoreClient<Station> storeClient) {
        super(clientHandler, cacheLoader, serverLoader, storeClient);
    }
}
