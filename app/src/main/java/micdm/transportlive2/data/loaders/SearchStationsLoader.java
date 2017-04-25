package micdm.transportlive2.data.loaders;

import java.util.ArrayList;
import java.util.Collection;

import javax.inject.Inject;
import javax.inject.Named;

import io.reactivex.Observable;
import io.reactivex.Scheduler;
import micdm.transportlive2.data.loaders.remote.SearchStationsResponse;
import micdm.transportlive2.data.loaders.remote.ServerConnector;
import micdm.transportlive2.misc.IdFactory;
import micdm.transportlive2.models.ImmutablePoint;
import micdm.transportlive2.models.ImmutableStation;
import micdm.transportlive2.models.Station;

public class SearchStationsLoader {

    @Inject
    IdFactory idFactory;
    @Inject
    @Named("io")
    Scheduler ioScheduler;
    @Inject
    ServerConnector serverConnector;

    public Observable<Result<Collection<Station>>> load(String query) {
        return serverConnector.searchStations(query)
            .toObservable()
            .map(this::convert)
            .map(Result::newSuccess)
            .onErrorReturn(error -> Result.newFail())
            .startWith(Result.newLoading())
            .subscribeOn(ioScheduler);
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
