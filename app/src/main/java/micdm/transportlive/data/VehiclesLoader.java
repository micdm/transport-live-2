package micdm.transportlive.data;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;
import javax.inject.Named;

import io.reactivex.Observable;
import io.reactivex.Scheduler;
import micdm.transportlive.misc.Irrelevant;
import micdm.transportlive.models.ImmutableRoute;
import micdm.transportlive.models.ImmutableRouteGroup;
import micdm.transportlive.models.ImmutableVehicle;
import micdm.transportlive.models.Route;
import micdm.transportlive.models.RouteGroup;
import micdm.transportlive.models.Vehicle;
import micdm.transportlive.utils.ObservableCache;
import timber.log.Timber;

public class VehiclesLoader extends BaseLoader<VehiclesLoader.Client> implements RoutesLoader.Client {

    public interface Client {

        Observable<Collection<String>> getLoadVehiclesRequests();
        Observable<Collection<String>> getReloadVehiclesRequests();
    }

    @Inject
    @Named("io")
    Scheduler ioScheduler;
    @Inject
    ObservableCache observableCache;
    @Inject
    RoutesLoader routesLoader;
    @Inject
    ServerConnector serverConnector;

    @Override
    public void init() {
        routesLoader.attach(this);
    }

    @Override
    public Observable<State> getData() {
        return observableCache.get(this, "getData",
            Observable
                .merge(
                    getLoadRequests().map(o -> new StateLoading()),
                    getLoadRequests()
                        .switchMap(this::loadRoutes)
                        .flatMap(o ->
                            routesLoader.getData()
                                .ofType(StateSuccess.class)
                                .take(1)
                                .map(state  -> state.routeGroups),
                            this::putRoutesIntoGroups
                        )
                        .<State>map(StateSuccess::new)
                        .retry(3)
                        .doOnError(error ->
                            Timber.w(error, "Cannot load vehicles")
                        )
                        .onErrorReturn(o -> new StateFail())
                        .subscribeOn(ioScheduler),
                    routesLoader.getData()
                        .ofType(StateFail.class)
                        .map(o -> new StateFail())
                )
                .replay(1)
                .autoConnect()
        );
    }

    private Observable<Collection<String>> getLoadRequests() {
        return Observable
            .merge(
                clients.get().flatMap(Client::getLoadVehiclesRequests).distinctUntilChanged(),
                clients.get().flatMap(Client::getReloadVehiclesRequests)
            )
            .debounce(50, TimeUnit.MILLISECONDS);
    }

    private Observable<Collection<Route>> loadRoutes(Collection<String> ids) {
        List<Observable<Route>> observables = new ArrayList<>();
        for (String routeId: ids) {
            observables.add(
                serverConnector.getVehicles(routeId)
                    .toObservable()
                    .flatMap(o ->
                        routesLoader.getData()
                            .ofType(StateSuccess.class)
                            .take(1)
                            .map(state -> state.routeGroups),
                        (response, groups) -> {
                            RouteGroup group = getGroupByRoute(groups.values(), routeId);
                            ImmutableRoute.Builder builder = ImmutableRoute.builder().from(group.routes().get(routeId));
                            for (GetVehiclesResponse item: response) {
                                Vehicle vehicle = ImmutableVehicle.builder()
                                    .id(item.Auto.AutoId)
                                    .latitude(item.Point.Lat)
                                    .longitude(item.Point.Lon)
                                    .direction(item.Point.Dir)
                                    .build();
                                builder.putVehicles(vehicle.id(), vehicle);
                            }
                            return builder.build();
                        }
                    )
            );
        }
        return Observable.combineLatest(observables, (Object... routes) -> {
            Collection<Route> result = new ArrayList<>();
            for (Object route: routes) {
                result.add((Route) route);
            }
            return result;
        });
    }

    private RouteGroup getGroupByRoute(Collection<RouteGroup> groups, String routeId) {
        for (RouteGroup group: groups) {
            if (group.routes().containsKey(routeId)) {
                return group;
            }
        }
        throw new IllegalStateException(String.format("unknown route %s", routeId));
    }

    private Map<String, RouteGroup> putRoutesIntoGroups(Collection<Route> routes, Map<String, RouteGroup> groups) {
        Map<String, ImmutableRouteGroup.Builder> builders = new HashMap<>();
        for (Route route: routes) {
            RouteGroup group = getGroupByRoute(groups.values(), route.id());
            ImmutableRouteGroup.Builder builder = builders.get(group.id());
            if (builder == null) {
                builder = ImmutableRouteGroup.builder().from(group);
                builders.put(group.id(), builder);
            }
            builder.putRoutes(route.id(), route);
        }
        Map<String, RouteGroup> result = new HashMap<>(groups);
        for (Map.Entry<String, ImmutableRouteGroup.Builder> entry: builders.entrySet()) {
            result.put(entry.getKey(), entry.getValue().build());
        }
        return result;
    }

    @Override
    public Observable<Object> getLoadRoutesRequests() {
        return clients.get().flatMap(Client::getLoadVehiclesRequests).take(1).map(o -> Irrelevant.INSTANCE);
    }

    @Override
    public Observable<Object> getReloadRoutesRequests() {
        return clients.get().flatMap(Client::getReloadVehiclesRequests).map(o -> Irrelevant.INSTANCE);
    }
}
