package micdm.transportlive2.data.stores;

import java.util.Collection;

import micdm.transportlive2.models.RouteGroup;

public class RoutesStore extends BaseStore<Collection<RouteGroup>> {

    RoutesStore(Adapter<Collection<RouteGroup>> adapter, Storage storage) {
        super(adapter, storage);
    }
}
