package micdm.transportlive.data;

import java.util.List;

import javax.inject.Inject;

import io.reactivex.Observable;
import micdm.transportlive.misc.CommonFunctions;
import micdm.transportlive.misc.Irrelevant;
import micdm.transportlive.models.ImmutablePath;
import micdm.transportlive.models.ImmutablePoint;
import micdm.transportlive.models.Path;

public class PathLoader extends DefaultLoader<PathLoader.Client, Path> implements PathsStore.Client {

    public interface Client {

        Observable<String> getLoadPathRequests();
        Observable<String> getReloadPathRequests();
        Observable<String> getCancelPathLoadingRequests();
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
    public void init() {
        pathsStore.attach(this);
    }

    @Override
    protected Observable<Object> getLoadRequests() {
        return clients.get()
            .flatMap(Client::getLoadPathRequests)
            .filter(commonFunctions.isEqual(routeId))
            .compose(commonFunctions.toConst(Irrelevant.INSTANCE));
    }

    @Override
    Observable<Object> getReloadRequests() {
        return clients.get()
            .flatMap(Client::getReloadPathRequests)
            .filter(commonFunctions.isEqual(routeId))
            .compose(commonFunctions.toConst(Irrelevant.INSTANCE));
    }

    @Override
    protected Observable<Object> getCancelRequests() {
        return clients.get()
            .flatMap(Client::getCancelPathLoadingRequests)
            .filter(commonFunctions.isEqual(routeId))
            .compose(commonFunctions.toConst(Irrelevant.INSTANCE));
    }

    @Override
    protected Observable<Path> loadFromCache() {
        return pathsStore.getData(routeId);
    }

    @Override
    protected Observable<Path> loadFromServer() {
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
