package micdm.transportlive2.data.loaders;

import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;

import micdm.transportlive2.ComponentHolder;
import micdm.transportlive2.data.loaders.remote.ServerConnector;
import micdm.transportlive2.data.stores.Stores;
import micdm.transportlive2.misc.CommonFunctions;
import micdm.transportlive2.misc.Container;
import micdm.transportlive2.misc.Id;
import micdm.transportlive2.misc.IdFactory;

public class Loaders extends Container<BaseLoader> {

    @Inject
    CommonFunctions commonFunctions;
    @Inject
    IdFactory idFactory;
    @Inject
    RoutesLoader routesLoader;
    @Inject
    ServerConnector serverConnector;
    @Inject
    Stores stores;

    private final Map<Id, PathLoader> pathLoaders = new HashMap<>();
    private final Map<Id, VehiclesLoader> vehiclesLoaders = new HashMap<>();
    private final Map<Id, ForecastLoader> forecastLoaders = new HashMap<>();

    public ForecastLoader getForecastLoader(Id stationId) {
        return getOrCreateInstance(forecastLoaders, stationId, () -> {
            ForecastLoader instance = new ForecastLoader(
                clients -> clients.get().flatMap(ForecastLoader.Client::getLoadForecastRequests),
                new DummyCacheLoader<>(),
                new ForecastLoader.ForecastServerLoader(idFactory, serverConnector, stationId),
                new DummyStoreClient<>()
            );
            ComponentHolder.getAppComponent().inject(instance);
            return instance;
        });
    }

    public RoutesLoader getRoutesLoader() {
        return routesLoader;
    }

    public PathLoader getPathLoader(Id routeId) {
        return getOrCreateInstance(pathLoaders, routeId, () -> {
            PathLoader instance = new PathLoader(
                clients -> clients.get()
                    .flatMap(PathLoader.Client::getLoadPathRequests)
                    .filter(routeId::equals)
                    .compose(commonFunctions.toNothing()),
                new StoreCacheLoader<>(stores.getPathStore(routeId)),
                new PathLoader.PathServerLoader(idFactory, serverConnector, routeId),
                new PathLoader.PathStoreClient(stores.getPathStore(routeId))
            );
            ComponentHolder.getAppComponent().inject(instance);
            return instance;
        });
    }

    public VehiclesLoader getVehiclesLoader(Id routeId) {
        return getOrCreateInstance(vehiclesLoaders, routeId, () -> {
            VehiclesLoader instance = new VehiclesLoader(
                clients -> clients.get()
                    .flatMap(VehiclesLoader.Client::getLoadVehiclesRequests)
                    .filter(routeId::equals)
                    .compose(commonFunctions.toNothing()),
                new DummyCacheLoader<>(),
                new VehiclesLoader.VehiclesServerLoader(idFactory, serverConnector, routeId),
                new DummyStoreClient<>()
            );
            ComponentHolder.getAppComponent().inject(instance);
            return instance;
        });
    }

    @Override
    protected void onNewInstance(BaseLoader instance) {
        instance.init();
    }
}
