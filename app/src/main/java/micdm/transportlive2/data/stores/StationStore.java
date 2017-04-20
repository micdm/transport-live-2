package micdm.transportlive2.data.stores;

import io.reactivex.Observable;
import micdm.transportlive2.models.Station;

public class StationStore extends BaseStore<StationStore.Client, Station> {

    public interface Client {

        Observable<Station> getStoreStationRequests();
    }

    StationStore(ClientHandler<Client, Station> clientHandler, Adapter<Station> adapter, Storage storage) {
        super(clientHandler, adapter, storage);
    }
}
