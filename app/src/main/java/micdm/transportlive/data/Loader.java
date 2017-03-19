package micdm.transportlive.data;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Named;

import io.reactivex.Observable;
import io.reactivex.Scheduler;
import io.reactivex.Single;
import io.reactivex.subjects.BehaviorSubject;
import io.reactivex.subjects.PublishSubject;
import io.reactivex.subjects.Subject;
import micdm.transportlive.models.ImmutableRoute;
import micdm.transportlive.models.ImmutableRouteGroup;
import micdm.transportlive.models.ImmutableVehicle;
import micdm.transportlive.models.Route;
import micdm.transportlive.models.RouteGroup;
import micdm.transportlive.models.Vehicle;
import timber.log.Timber;

public class Loader {

    public enum LoadingState {
        START,
        SUCCESS,
        FAIL
    }

    private static abstract class Task<T> {

        private final Subject<LoadingState> state = BehaviorSubject.create();
        private final Subject<T> result = BehaviorSubject.create();

        Observable<LoadingState> getState() {
            return state;
        }

        Observable<T> getResult() {
            return result;
        }

        void execute() {
            Timber.d("Executing task %s", this);
            state.onNext(LoadingState.START);
            load().subscribe(response -> {
                Timber.d("Task %s completed", this);
                result.onNext(response);
                state.onNext(LoadingState.SUCCESS);
            }, error -> {
                Timber.d(error, "Task %s failed", this);
                state.onNext(LoadingState.FAIL);
            });
        }

        abstract Single<T> load();
    }

    private static class GetRoutesTask extends Task<Set<GetRoutesResponse>> {

        private final ServerConnector serverConnector;

        private GetRoutesTask(ServerConnector serverConnector) {
            this.serverConnector = serverConnector;
        }

        @Override
        Single<Set<GetRoutesResponse>> load() {
            return serverConnector.getRoutes();
        }
    }

    private static class GetVehiclesTask extends Task<Set<GetVehiclesResponse>> {

        private final ServerConnector serverConnector;

        final String routeId;

        GetVehiclesTask(ServerConnector serverConnector, String routeId) {
            this.serverConnector = serverConnector;
            this.routeId = routeId;
        }

        @Override
        Single<Set<GetVehiclesResponse>> load() {
            return serverConnector.getVehicles(routeId);
        }
    }

    @Inject
    DataRepository dataRepository;
    @Inject
    @Named("io")
    Scheduler ioScheduler;
    @Inject
    @Named("mainThread")
    Scheduler mainThreadScheduler;
    @Inject
    ServerConnector serverConnector;

    private final Subject<Task> tasks = PublishSubject.create();

    void init() {
        Observable
            .merge(
                tasks
                    .ofType(GetRoutesTask.class)
                    .doOnNext(Task::execute)
                    .switchMap(task ->
                        task.getResult()
                            .map(this::convertRoutesResponseToData)
                    ),
                tasks
                    .ofType(GetVehiclesTask.class)
                    .doOnNext(Task::execute)
                    .switchMap(task ->
                        task.getResult()
                            .switchMap(response -> loadRoutes().map(o -> response))
                            .withLatestFrom(dataRepository.getRouteGroups(), Observable.just(task.routeId), this::convertVehiclesResponseToData)
                    )
            )
            .subscribe(dataRepository::putRouteGroups);
    }

    private Map<String, RouteGroup> convertRoutesResponseToData(Set<GetRoutesResponse> response) {
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

    private Map<String, RouteGroup> convertVehiclesResponseToData(Set<GetVehiclesResponse> response, Map<String, RouteGroup> groups, String routeId) {
        RouteGroup group = getGroupByRoute(groups.values(), routeId);
        if (group == null) {
            throw new IllegalStateException(String.format("unknown route %s", routeId));
        }
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
        Route route = builder.build();
        groups.put(group.id(),
            ImmutableRouteGroup.builder()
                .from(group)
                .putRoutes(route.id(), route)
                .build()
        );
        return groups;
    }

    private RouteGroup getGroupByRoute(Collection<RouteGroup> groups, String routeId) {
        for (RouteGroup group: groups) {
            if (group.routes().containsKey(routeId)) {
                return group;
            }
        }
        return null;
    }

    public Observable<LoadingState> loadRoutes() {
        return addTask(new GetRoutesTask(serverConnector));
    }

    public Observable<LoadingState> loadVehicles(String routeId) {
        return addTask(new GetVehiclesTask(serverConnector, routeId));
    }

    private Observable<LoadingState> addTask(Task<?> task) {
        return task.getState()
            .doOnSubscribe(o -> tasks.onNext(task))
            .subscribeOn(ioScheduler)
            .observeOn(mainThreadScheduler);
    }
}
