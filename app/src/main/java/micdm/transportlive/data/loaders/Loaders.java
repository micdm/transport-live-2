package micdm.transportlive.data.loaders;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import micdm.transportlive.ComponentHolder;

public class Loaders {

    private final Lock lock = new ReentrantLock();

    private RoutesLoader routesLoader;
    private final Map<String, PathLoader> pathLoaders = new HashMap<>();
    private final Map<String, VehiclesLoader> vehiclesLoaders = new HashMap<>();

    public RoutesLoader getRoutesLoader() {
        lock.lock();
        try {
            if (routesLoader == null) {
                routesLoader = new RoutesLoader();
                ComponentHolder.getAppComponent().inject(routesLoader);
                routesLoader.init();
            }
            return routesLoader;
        } finally {
            lock.unlock();
        }
    }

    public PathLoader getPathLoader(String routeId) {
        lock.lock();
        try {
            PathLoader loader = pathLoaders.get(routeId);
            if (loader == null) {
                loader = new PathLoader(routeId);
                ComponentHolder.getAppComponent().inject(loader);
                loader.init();
                pathLoaders.put(routeId, loader);
            }
            return loader;
        } finally {
            lock.unlock();
        }
    }

    public VehiclesLoader getVehiclesLoader(String routeId) {
        lock.lock();
        try {
            VehiclesLoader loader = vehiclesLoaders.get(routeId);
            if (loader == null) {
                loader = new VehiclesLoader(routeId);
                ComponentHolder.getAppComponent().inject(loader);
                loader.init();
                vehiclesLoaders.put(routeId, loader);
            }
            return loader;
        } finally {
            lock.unlock();
        }
    }
}
