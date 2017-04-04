package micdm.transportlive.data.loaders;

import java.util.List;

import javax.inject.Inject;

import io.reactivex.Observable;
import micdm.transportlive.data.loaders.remote.GetPathResponse;
import micdm.transportlive.data.loaders.remote.ServerConnector;
import micdm.transportlive.data.stores.PathsStore;
import micdm.transportlive.misc.CommonFunctions;
import micdm.transportlive.misc.Irrelevant;
import micdm.transportlive.models.ImmutablePath;
import micdm.transportlive.models.ImmutablePoint;
import micdm.transportlive.models.Path;

public class PathLoader extends DefaultLoader<PathLoader.Client, Path> implements PathsStore.Client {

    public interface Client {

        Observable<String> getLoadPathRequests();
    }

    @Inject
    CommonFunctions commonFunctions;
    @Inject
    PathsStore pathsStore;
    @Inject
    ServerConnector serverConnector;

    private final String routeId;

    PathLoader(String routeId) {
        this.routeId = routeId;
    }

    @Override
    public String toString() {
        return String.format("PathLoader(routeId=%s)", routeId);
    }

    @Override
    void init() {
        pathsStore.attach(this);
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
        return pathsStore.getData(routeId);
    }

    @Override
    Observable<Path> loadFromServer() {
        return serverConnector.getPath(routeId)
            .toObservable()
            .map(response -> {
                ImmutablePath.Builder builder = ImmutablePath.builder().route(routeId);
                for (GetPathResponse.SegmentsGeoJson.Feature feature: response.SegmentsGeoJson.features) {
                    for (List<Float> values: feature.geometry.coordinates){
                        builder.addPoints(
                            ImmutablePoint.builder()
                                .latitude(values.get(1))
                                .longitude(values.get(0))
                                .build()
                        );
                    }
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
