package micdm.transportlive.data;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;
import javax.inject.Named;

import io.reactivex.Observable;
import io.reactivex.Scheduler;
import micdm.transportlive.models.ImmutableRoute;
import micdm.transportlive.models.ImmutableRouteGroup;
import micdm.transportlive.models.RouteGroup;
import micdm.transportlive.utils.ObservableCache;
import timber.log.Timber;

public class RoutesLoader extends BaseLoader<RoutesLoader.Client> {

    public interface Client {

        Observable<Object> getLoadRoutesRequests();
        Observable<Object> getReloadRoutesRequests();
    }

    @Inject
    @Named("io")
    Scheduler ioScheduler;
    @Inject
    ObservableCache observableCache;
    @Inject
    ServerConnector serverConnector;

    @Override
    public Observable<State> getData() {
        return observableCache.get(this, "getData",
            Observable
                .merge(
                    getLoadRequests().map(o -> new StateLoading()),
                    getLoadRequests()
                        .switchMap(o -> serverConnector.getRoutes().toObservable())
                        .map(this::convertRoutesResponseToData)
                        .<State>map(StateSuccess::new)
                        .retry(3)
                        .doOnError(error ->
                            Timber.w(error, "Cannot load routes")
                        )
                        .onErrorReturn(o -> new StateFail())
                        .subscribeOn(ioScheduler)
                )
                .replay(1)
                .autoConnect()
        );
    }

    private Observable<Object> getLoadRequests() {
        return Observable
            .merge(
                clients.get().flatMap(Client::getLoadRoutesRequests).take(1),
                clients.get().flatMap(Client::getReloadRoutesRequests)
            )
            .debounce(50, TimeUnit.MILLISECONDS);
    }

    private Map<String, RouteGroup> convertRoutesResponseToData(Collection<GetRoutesResponse> response) {
        Map<String, ImmutableRouteGroup.Builder> builders = new HashMap<>();
        for (GetRoutesResponse item: response) {
            String groupId = item.PathwayGroup.PathwayGroupId;
            ImmutableRouteGroup.Builder groupBuilder = builders.get(groupId);
            if (groupBuilder == null) {
                groupBuilder = ImmutableRouteGroup.builder()
                    .id(groupId)
                    .name(item.PathwayGroup.Name);
                builders.put(groupId, groupBuilder);
            }
            String routeId = item.PathwayId;
            groupBuilder.putRoutes(routeId,
                ImmutableRoute.builder()
                    .id(routeId)
                    .source(item.ItineraryFrom)
                    .destination(item.ItineraryTo)
                    .number(item.Number)
                    .build()
            );
        }
        Map<String, RouteGroup> groups = new HashMap<>();
        for (Map.Entry<String, ImmutableRouteGroup.Builder> entry: builders.entrySet()) {
            groups.put(entry.getKey(), entry.getValue().build());
        }
        return groups;
    }
}
