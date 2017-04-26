package micdm.transportlive2.data.loaders;

import java.util.ArrayList;
import java.util.Collection;

import io.reactivex.Single;
import micdm.transportlive2.data.loaders.remote.SearchStationsResponse;
import micdm.transportlive2.data.loaders.remote.ServerConnector;
import micdm.transportlive2.misc.IdFactory;
import micdm.transportlive2.models.ImmutablePoint;
import micdm.transportlive2.models.ImmutableStation;
import micdm.transportlive2.models.Station;

public class SearchStationsLoader extends BaseLoader<Collection<Station>> {

    static class SearchStationsServerLoader implements ServerLoader<Collection<Station>> {

        private final IdFactory idFactory;
        private final ServerConnector serverConnector;
        private final String query;

        SearchStationsServerLoader(IdFactory idFactory, ServerConnector serverConnector, String query) {
            this.idFactory = idFactory;
            this.serverConnector = serverConnector;
            this.query = query;
        }

        @Override
        public Single<Collection<Station>> load() {
            return serverConnector.searchStations(query).map(this::convert);
        }

        private Collection<Station> convert(SearchStationsResponse response) {
            Collection<Station> result = new ArrayList<>(response.PathwayStopPoints.size());
            for (SearchStationsResponse.PathwayStopPoint item: response.PathwayStopPoints) {
                result.add(
                    ImmutableStation.builder()
                        .id(idFactory.newInstance(item.PathwayMilestoneId))
                        .name(item.LegalName)
                        .description(item.Description)
                        .location(
                            ImmutablePoint.builder()
                                .latitude(item.Latitude)
                                .longitude(item.Longitude)
                                .build()
                        )
                        .build()
                );
            }
            return result;
        }
    }

    SearchStationsLoader(CacheClient<Collection<Station>> cacheClient, ServerLoader<Collection<Station>> serverLoader) {
        super(cacheClient, serverLoader);
    }
}
