package micdm.transportlive2.data.loaders;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import io.reactivex.Observable;
import io.reactivex.Single;
import micdm.transportlive2.data.loaders.remote.GetRoutesResponse;
import micdm.transportlive2.data.loaders.remote.ServerConnector;
import micdm.transportlive2.data.stores.BaseStore;
import micdm.transportlive2.data.stores.RoutesStore;
import micdm.transportlive2.misc.IdFactory;
import micdm.transportlive2.models.ImmutableRoute;
import micdm.transportlive2.models.ImmutableRouteGroup;
import micdm.transportlive2.models.RouteGroup;

public class RoutesLoader extends BaseLoader<RoutesLoader.Client, Collection<RouteGroup>> {

    public interface Client {

        Observable<Object> getLoadRoutesRequests();
    }

    static class RoutesServerLoader implements BaseLoader.ServerLoader<Collection<RouteGroup>> {

        private final IdFactory idFactory;
        private final ServerConnector serverConnector;

        RoutesServerLoader(IdFactory idFactory, ServerConnector serverConnector) {
            this.idFactory = idFactory;
            this.serverConnector = serverConnector;
        }

        @Override
        public Single<Collection<RouteGroup>> load() {
            return serverConnector.getRoutes().map(this::convert);
        }

        Collection<RouteGroup> convert(Collection<GetRoutesResponse> response) {
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
    }

    static class RoutesStoreClient extends DefaultStoreClient<RoutesStore.Client, Collection<RouteGroup>> implements RoutesStore.Client {

        RoutesStoreClient(BaseStore<RoutesStore.Client, Collection<RouteGroup>> store) {
            super(store);
        }

        @Override
        public void attach() {
            store.attach(this);
        }

        @Override
        public Observable<Collection<RouteGroup>> getStoreRoutesRequests() {
            return data;
        }
    }

    RoutesLoader(ClientHandler<Client> clientHandler, CacheLoader<Collection<RouteGroup>> cacheLoader,
                 ServerLoader<Collection<RouteGroup>> serverLoader, StoreClient<Collection<RouteGroup>> storeClient) {
        super(clientHandler, cacheLoader, serverLoader, storeClient);
    }
}
