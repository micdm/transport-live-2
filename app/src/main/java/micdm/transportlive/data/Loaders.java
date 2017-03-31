package micdm.transportlive.data;

import java.util.HashMap;
import java.util.Map;

import micdm.transportlive.ComponentHolder;

public class Loaders {

    private RoutesLoader routesLoader;
    private final Map<String, PathLoader> pathLoaders = new HashMap<>();
    private final Map<String, VehiclesLoader> vehiclesLoaders = new HashMap<>();

    public RoutesLoader getRoutesLoader() {
        if (routesLoader == null) {
            routesLoader = new RoutesLoader();
            ComponentHolder.getAppComponent().inject(routesLoader);
            routesLoader.init();
        }
        return routesLoader;
    }

    public PathLoader getPathLoader(String routeId) {
        PathLoader loader = pathLoaders.get(routeId);
        if (loader == null) {
            loader = new PathLoader(routeId);
            ComponentHolder.getAppComponent().inject(loader);
            loader.init();
            pathLoaders.put(routeId, loader);
        }
        return loader;
    }

    public VehiclesLoader getVehiclesLoader(String routeId) {
        VehiclesLoader loader = vehiclesLoaders.get(routeId);
        if (loader == null) {
            loader = new VehiclesLoader(routeId);
            ComponentHolder.getAppComponent().inject(loader);
            loader.init();
            vehiclesLoaders.put(routeId, loader);
        }
        return loader;
    }
}
