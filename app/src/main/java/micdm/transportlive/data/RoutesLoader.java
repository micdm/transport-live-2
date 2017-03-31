package micdm.transportlive.data;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;

import io.reactivex.Observable;
import micdm.transportlive.misc.Irrelevant;
import micdm.transportlive.models.ImmutableRoute;
import micdm.transportlive.models.ImmutableRouteGroup;
import micdm.transportlive.models.RouteGroup;

public class RoutesLoader extends DefaultLoader<RoutesLoader.Client, Collection<RouteGroup>> implements RoutesStore.Client {

    public interface Client {

        Observable<Object> getLoadRoutesRequests();
        Observable<Object> getReloadRoutesRequests();
        Observable<Object> getCancelRoutesLoadingRequests();
    }

    @Inject
    RoutesStore routesStore;
    @Inject
    ServerConnector serverConnector;

    @Override
    public void init() {
        routesStore.attach(this);
    }

    @Override
    protected Observable<Object> getLoadRequests() {
        return clients.get()
            .flatMap(Client::getLoadRoutesRequests)
            .compose(commonFunctions.toConst(Irrelevant.INSTANCE));
    }

    @Override
    Observable<Object> getReloadRequests() {
        return clients.get()
            .flatMap(Client::getReloadRoutesRequests)
            .compose(commonFunctions.toConst(Irrelevant.INSTANCE));
    }

    @Override
    protected Observable<Object> getCancelRequests() {
        return clients.get()
            .flatMap(Client::getCancelRoutesLoadingRequests)
            .compose(commonFunctions.toConst(Irrelevant.INSTANCE));
    }

    @Override
    Observable<Collection<RouteGroup>> loadFromCache() {
        return routesStore.getData(""); //TODO: туповато
    }

    @Override
    Observable<Collection<RouteGroup>> loadFromServer() {
        return serverConnector.getRoutes()
            .toObservable()
            .map(response -> {
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
                    groupBuilder.addRoutes(
                        ImmutableRoute.builder()
                            .id(routeId)
                            .source(item.ItineraryFrom)
                            .destination(item.ItineraryTo)
                            .number(item.Number)
                            .build()
                    );
                }
                Collection<RouteGroup> groups = new ArrayList<>();
                for (ImmutableRouteGroup.Builder builder: builders.values()) {
                    groups.add(builder.build());
                }
                return groups;
            });
    }

    @Override
    public Observable<Collection<RouteGroup>> getStoreRoutesRequests() {
        return getData()
            .filter(Result::isSuccess)
            .map(Result::getData);
    }
}
