package micdm.transportlive2.data.loaders;

import org.joda.time.Duration;

import javax.inject.Inject;

import io.reactivex.Observable;
import micdm.transportlive2.data.loaders.remote.GetForecastResponse;
import micdm.transportlive2.data.loaders.remote.ServerConnector;
import micdm.transportlive2.misc.CommonFunctions;
import micdm.transportlive2.misc.Id;
import micdm.transportlive2.misc.IdFactory;
import micdm.transportlive2.models.Forecast;
import micdm.transportlive2.models.ImmutableForecast;

public class ForecastLoader extends DefaultLoader<ForecastLoader.Client, Forecast> {

    public interface Client {

        Observable<Object> getLoadForecastRequests();
    }

    @Inject
    CommonFunctions commonFunctions;
    @Inject
    IdFactory idFactory;
    @Inject
    ServerConnector serverConnector;

    private final Id stationId;

    ForecastLoader(Id stationId) {
        this.stationId = stationId;
    }

    @Override
    public String toString() {
        return String.format("ForecastLoader(stationId=%s)", stationId);
    }

    @Override
    Observable<Object> getLoadRequests() {
        return clients.get().flatMap(Client::getLoadForecastRequests);
    }

    @Override
    Observable<Forecast> loadFromCache() {
        return Observable.empty();
    }

    @Override
    Observable<Forecast> loadFromServer() {
        return serverConnector.getForecast(stationId)
            .toObservable()
            .map(response -> {
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
            });
    }
}
