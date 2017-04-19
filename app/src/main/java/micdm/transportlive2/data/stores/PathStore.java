package micdm.transportlive2.data.stores;

import io.reactivex.Observable;
import micdm.transportlive2.models.Path;

public class PathStore extends BaseStore<PathStore.Client, Path> {

    public interface Client {

        Observable<Path> getStorePathRequests();
    }

    PathStore(ClientHandler<Client, Path> clientHandler, Adapter<Path> adapter, Storage storage) {
        super(clientHandler, adapter, storage);
    }
}
