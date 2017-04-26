package micdm.transportlive2.data.loaders;

import io.reactivex.Single;
import micdm.transportlive2.data.loaders.remote.GetStationResponse;
import micdm.transportlive2.data.loaders.remote.ServerConnector;
import micdm.transportlive2.misc.Id;
import micdm.transportlive2.misc.IdFactory;
import micdm.transportlive2.models.ImmutablePoint;
import micdm.transportlive2.models.ImmutableStation;
import micdm.transportlive2.models.Station;

public class StationLoader extends BaseLoader<Station> {

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

    StationLoader(CacheClient<Station> cacheClient, ServerLoader<Station> serverLoader) {
        super(cacheClient, serverLoader);
    }
}
