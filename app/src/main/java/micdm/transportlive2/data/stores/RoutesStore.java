package micdm.transportlive2.data.stores;

import java.util.Collection;

import io.reactivex.Observable;
import micdm.transportlive2.models.RouteGroup;

public class RoutesStore extends BaseStore<RoutesStore.Client, Collection<RouteGroup>> {

    public interface Client {

        Observable<Collection<RouteGroup>> getStoreRoutesRequests();
    }

    RoutesStore(ClientHandler<Client, Collection<RouteGroup>> clientHandler, Adapter<Collection<RouteGroup>> adapter, Storage storage) {
        super(clientHandler, adapter, storage);
    }
}
