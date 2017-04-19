package micdm.transportlive2.data.stores;

import io.reactivex.Observable;
import micdm.transportlive2.misc.Clients;
import timber.log.Timber;

class BaseStore<Client, Data> {

    interface ClientHandler<Client, Data> {

        Observable<Data> getWriteRequests(Clients<Client> clients);
    }

    interface Adapter<Data> {

        String serialize(Data data);
        Data deserialize(String serialized);
    }

    interface Storage {

        String read();
        void write(String value);
    }

    private final Clients<Client> clients = new Clients<>();

    private final ClientHandler<Client, Data> clientHandler;
    private final Adapter<Data> adapter;
    private final Storage storage;
    private final Data initial;

    BaseStore(ClientHandler<Client, Data> clientHandler, Adapter<Data> adapter, Storage storage) {
        this(clientHandler, adapter, storage, null);
    }

    BaseStore(ClientHandler<Client, Data> clientHandler, Adapter<Data> adapter, Storage storage, Data initial) {
        this.clientHandler = clientHandler;
        this.adapter = adapter;
        this.storage = storage;
        this.initial = initial;
    }

    void init() {
        subscribeForWriteRequests();
    }

    private void subscribeForWriteRequests() {
        clientHandler.getWriteRequests(clients).subscribe(data ->
            storage.write(adapter.serialize(data))
        );
    }

    public Observable<Data> getData() {
        return clientHandler.getWriteRequests(clients)
            .startWith(getStored())
            .distinctUntilChanged();
    }

    public Observable<Data> getStored() {
        String serialized = storage.read();
        if (serialized != null) {
            try {
                return Observable.just(adapter.deserialize(serialized));
            } catch (Exception e) {
                Timber.w(e, "Cannot deserialize data");
            }
        }
        return initial == null ? Observable.empty() : Observable.just(initial);
    }

    public void attach(Client client) {
        clients.attach(client);
    }

    public void detach(Client client) {
        clients.detach(client);
    }
}
