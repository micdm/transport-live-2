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

    private final Map<Id, ForecastLoader> forecastLoaders = new HashMap<>();
    private final Map<Id, PathLoader> pathLoaders = new HashMap<>();
    private final Map<String, SearchStationsLoader> searchStationsLoaders = new HashMap<>();
    private final Map<Id, StationLoader> stationLoaders = new HashMap<>();
    private final Map<Id, VehiclesLoader> vehiclesLoaders = new HashMap<>();

    public ForecastLoader getForecastLoader(Id stationId) {
        return getOrCreateInstance(forecastLoaders, stationId, () -> {
            ForecastLoader instance = new ForecastLoader(
                new DummyCacheClient<>(),
                new ForecastLoader.ForecastServerLoader(idFactory, serverConnector, stationId)
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
                new DefaultCacheClient<>(stores.getPathStore(routeId)),
                new PathLoader.PathServerLoader(idFactory, serverConnector, routeId)
            );
            ComponentHolder.getAppComponent().inject(instance);
            return instance;
        });
    }

    public SearchStationsLoader getSearchStationsLoader(String query) {
        return getOrCreateInstance(searchStationsLoaders, query, () -> {
            SearchStationsLoader instance = new SearchStationsLoader(
                new DummyCacheClient<>(),
                new SearchStationsLoader.SearchStationsServerLoader(idFactory, serverConnector, query)
            );
            ComponentHolder.getAppComponent().inject(instance);
            return instance;
        });
    }

    public StationLoader getStationLoader(Id stationId) {
        return getOrCreateInstance(stationLoaders, stationId, () -> {
            StationLoader instance = new StationLoader(
                new DefaultCacheClient<>(stores.getStationStore(stationId)),
                new StationLoader.StationServerLoader(idFactory, serverConnector, stationId)
            );
            ComponentHolder.getAppComponent().inject(instance);
            return instance;
        });
    }

    public VehiclesLoader getVehiclesLoader(Id routeId) {
        return getOrCreateInstance(vehiclesLoaders, routeId, () -> {
            VehiclesLoader instance = new VehiclesLoader(
                new DummyCacheClient<>(),
                new VehiclesLoader.VehiclesServerLoader(idFactory, serverConnector, routeId)
            );
            ComponentHolder.getAppComponent().inject(instance);
            return instance;
        });
    }
}
