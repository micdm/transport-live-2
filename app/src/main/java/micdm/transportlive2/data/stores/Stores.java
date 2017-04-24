package micdm.transportlive2.data.stores;

import com.google.gson.Gson;

import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;

import micdm.transportlive2.misc.Cache;
import micdm.transportlive2.misc.Container;
import micdm.transportlive2.misc.Id;
import micdm.transportlive2.models.Path;
import micdm.transportlive2.models.Station;

public class Stores extends Container<BaseStore> {

    @Inject
    Cache cache;
    @Inject
    Gson gson;
    @Inject
    PreferencesStore preferencesStore;
    @Inject
    RoutesStore routesStore;

    private final Map<Id, PathStore> pathStores = new HashMap<>();
    private final Map<Id, StationStore> stationStores = new HashMap<>();

    public PathStore getPathStore(Id routeId) {
        return getOrCreateInstance(pathStores, routeId, () ->
            new PathStore(
                new BaseStore.Adapter<Path>() {
                    @Override
                    public String serialize(Path path) {
                        return gson.toJson(path, Path.class);
                    }
                    @Override
                    public Path deserialize(String serialized) {
                        return gson.fromJson(serialized, Path.class);
                    }
                },
                new CacheStorage(cache, String.format("path_%s", routeId.getOriginal()))
            )
        );
    }

    public PreferencesStore getPreferencesStore() {
        return preferencesStore;
    }

    public RoutesStore getRoutesStore() {
        return routesStore;
    }

    public StationStore getStationStore(Id stationId) {
        return getOrCreateInstance(stationStores, stationId, () ->
            new StationStore(
                new BaseStore.Adapter<Station>() {
                    @Override
                    public String serialize(Station station) {
                        return gson.toJson(station, Station.class);
                    }
                    @Override
                    public Station deserialize(String serialized) {
                        return gson.fromJson(serialized, Station.class);
                    }
                },
                new CacheStorage(cache, String.format("station_%s", stationId.getOriginal()))
            )
        );
    }

    @Override
    protected void onNewInstance(BaseStore instance) {
        instance.init();
    }
}
