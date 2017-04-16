package micdm.transportlive2.data.loaders;

import java.util.HashMap;
import java.util.Map;

import micdm.transportlive2.ComponentHolder;
import micdm.transportlive2.misc.Container;
import micdm.transportlive2.misc.Id;

public class Loaders extends Container<BaseLoader> {

    private final Map<Id, RoutesLoader> routesLoaders = new HashMap<>();
    private final Map<Id, PathLoader> pathLoaders = new HashMap<>();
    private final Map<Id, VehiclesLoader> vehiclesLoaders = new HashMap<>();
    private final Map<Id, ForecastLoader> forecastLoaders = new HashMap<>();

    public RoutesLoader getRoutesLoader() {
        // TODO: туповато
        return getOrCreateInstance(routesLoaders, null, () -> {
            RoutesLoader instance = new RoutesLoader();
            ComponentHolder.getAppComponent().inject(instance);
            return instance;
        });
    }

    public PathLoader getPathLoader(Id routeId) {
        return getOrCreateInstance(pathLoaders, routeId, () -> {
            PathLoader instance = new PathLoader(routeId);
            ComponentHolder.getAppComponent().inject(instance);
            return instance;
        });
    }

    public VehiclesLoader getVehiclesLoader(Id routeId) {
        return getOrCreateInstance(vehiclesLoaders, routeId, () -> {
            VehiclesLoader instance = new VehiclesLoader(routeId);
            ComponentHolder.getAppComponent().inject(instance);
            return instance;
        });
    }

    public ForecastLoader getForecastLoader(Id stationId) {
        return getOrCreateInstance(forecastLoaders, stationId, () -> {
            ForecastLoader instance = new ForecastLoader(stationId);
            ComponentHolder.getAppComponent().inject(instance);
            return instance;
        });
    }

    @Override
    protected void onNewInstance(BaseLoader instance) {
        instance.init();
    }
}
