package micdm.transportlive.data.loaders;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;

import io.reactivex.Observable;
import micdm.transportlive.data.loaders.remote.GetRoutesResponse;
import micdm.transportlive.data.loaders.remote.ServerConnector;
import micdm.transportlive.data.stores.RoutesStore;
import micdm.transportlive.misc.IdFactory;
import micdm.transportlive.misc.Irrelevant;
import micdm.transportlive.models.ImmutableRoute;
import micdm.transportlive.models.ImmutableRouteGroup;
import micdm.transportlive.models.RouteGroup;

public class RoutesLoader extends DefaultLoader<RoutesLoader.Client, Collection<RouteGroup>> implements RoutesStore.Client {

    public interface Client {

        Observable<Object> getLoadRoutesRequests();
    }

    @Inject
    IdFactory idFactory;
    @Inject
    RoutesStore routesStore;
    @Inject
    ServerConnector serverConnector;

    @Override
    public String toString() {
        return "RoutesLoader";
    }

    @Override
    void init() {
        routesStore.attach(this);
    }

    @Override
    Observable<Object> getLoadRequests() {
        return clients.get()
            .flatMap(Client::getLoadRoutesRequests)
            .compose(commonFunctions.toConst(Irrelevant.INSTANCE));
    }

    @Override
    Observable<Collection<RouteGroup>> loadFromCache() {
        return routesStore.getData(null); //TODO: туповато
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
                            .id(idFactory.newInstance(groupId))
                            .type(getRouteGroupType(groupId));
                        builders.put(groupId, groupBuilder);
                    }
                    String routeId = item.PathwayId;
                    groupBuilder.addRoutes(
                        ImmutableRoute.builder()
                            .id(idFactory.newInstance(routeId))
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

    private RouteGroup.Type getRouteGroupType(String id) {
        if (id.equals("a944360f-2771-4de0-a63a-4f3f2e628840")) {
            return RouteGroup.Type.TROLLEYBUS;
        }
        if (id.equals("093dcae3-e14d-4fc4-8f52-e1c869a1a087")) {
            return RouteGroup.Type.TRAM;
        }
        if (id.equals("cf0d566a-d077-40d7-8ad7-b6f1afa98de6"))  {
            return RouteGroup.Type.BUS;
        }
        throw new IllegalStateException(String.format("unknown routeId group %s", id));
    }

    @Override
    public Observable<Collection<RouteGroup>> getStoreRoutesRequests() {
        return getData()
            .filter(Result::isSuccess)
            .map(Result::getData);
    }
}
