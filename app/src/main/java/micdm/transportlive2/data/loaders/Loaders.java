package micdm.transportlive2.data.loaders;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import micdm.transportlive2.ComponentHolder;
import micdm.transportlive2.misc.Id;

public class Loaders {

    private final Lock lock = new ReentrantLock();

    private RoutesLoader routesLoader;
    private final Map<Id, PathLoader> pathLoaders = new HashMap<>();
    private final Map<Id, VehiclesLoader> vehiclesLoaders = new HashMap<>();

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

    public PathLoader getPathLoader(Id routeId) {
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

    public VehiclesLoader getVehiclesLoader(Id routeId) {
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
