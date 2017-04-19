package micdm.transportlive2.data.loaders;

import java.util.List;

import javax.inject.Inject;

import io.reactivex.Observable;
import micdm.transportlive2.data.loaders.remote.GetPathResponse;
import micdm.transportlive2.data.loaders.remote.ServerConnector;
import micdm.transportlive2.data.stores.PathStore;
import micdm.transportlive2.data.stores.Stores;
import micdm.transportlive2.misc.CommonFunctions;
import micdm.transportlive2.misc.Id;
import micdm.transportlive2.misc.IdFactory;
import micdm.transportlive2.misc.Irrelevant;
import micdm.transportlive2.models.ImmutablePath;
import micdm.transportlive2.models.ImmutablePoint;
import micdm.transportlive2.models.ImmutableStation;
import micdm.transportlive2.models.Path;

public class PathLoader extends DefaultLoader<PathLoader.Client, Path> implements PathStore.Client {

    public interface Client {

        Observable<Id> getLoadPathRequests();
    }

    @Inject
    CommonFunctions commonFunctions;
    @Inject
    IdFactory idFactory;
    @Inject
    ServerConnector serverConnector;
    @Inject
    Stores stores;

    private final Id routeId;

    PathLoader(Id routeId) {
        this.routeId = routeId;
    }

    @Override
    public String toString() {
        return String.format("PathLoader(routeId=%s)", routeId);
    }

    @Override
    void init() {
        stores.getPathStore(routeId).attach(this);
    }

    @Override
    Observable<Object> getLoadRequests() {
        return clients.get()
            .flatMap(Client::getLoadPathRequests)
            .filter(commonFunctions.isEqual(routeId))
            .compose(commonFunctions.toConst(Irrelevant.INSTANCE));
    }

    @Override
    Observable<Path> loadFromCache() {
        return stores.getPathStore(routeId).getStored();
    }

    @Override
    Observable<Path> loadFromServer() {
        return serverConnector.getPath(routeId)
            .toObservable()
            .map(response -> {
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
            });
    }

    @Override
    public Observable<Path> getStorePathRequests() {
        return getData()
            .filter(Result::isSuccess)
            .map(Result::getData);
    }
}
