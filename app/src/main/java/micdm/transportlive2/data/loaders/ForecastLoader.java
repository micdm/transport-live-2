package micdm.transportlive2.data.loaders;

import org.joda.time.Duration;

import io.reactivex.Observable;
import io.reactivex.Single;
import micdm.transportlive2.data.loaders.remote.GetForecastResponse;
import micdm.transportlive2.data.loaders.remote.ServerConnector;
import micdm.transportlive2.misc.Id;
import micdm.transportlive2.misc.IdFactory;
import micdm.transportlive2.models.Forecast;
import micdm.transportlive2.models.ImmutableForecast;

public class ForecastLoader extends BaseLoader<ForecastLoader.Client, Forecast> {

    public interface Client {

        Observable<Object> getLoadForecastRequests();
    }

    static class ForecastServerLoader implements ServerLoader<Forecast> {

        private final IdFactory idFactory;
        private final ServerConnector serverConnector;
        private final Id stationId;

        ForecastServerLoader(IdFactory idFactory, ServerConnector serverConnector, Id stationId) {
            this.idFactory = idFactory;
            this.serverConnector = serverConnector;
            this.stationId = stationId;
        }

        @Override
        public Single<Forecast> load() {
            return serverConnector.getForecast(stationId).map(this::convert);
        }

        private Forecast convert(GetForecastResponse response) {
            ImmutableForecast.Builder builder = ImmutableForecast.builder()
                .name(response.name)
                .description(response.desc == null ? "" : response.desc);
            for (GetForecastResponse.Forecast forecast: response.forecasts) {
                builder.addVehicles(
                    ImmutableForecast.Vehicle.builder()
                        .id(idFactory.newInstance(forecast.fid))
                        .routeId(idFactory.newInstance(forecast.pid))
                        .estimatedTime(Duration.standardSeconds(forecast.tba))
                        .build()
                );
            }
            return builder.build();
        }
    }

    ForecastLoader(ClientHandler<Client> clientHandler, CacheLoader<Forecast> cacheLoader,
                   ServerLoader<Forecast> serverLoader, StoreClient<Forecast> storeClient) {
        super(clientHandler, cacheLoader, serverLoader, storeClient);
    }
}
