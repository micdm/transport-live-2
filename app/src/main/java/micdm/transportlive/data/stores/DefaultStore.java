package micdm.transportlive.data.stores;

import javax.inject.Inject;

import io.reactivex.Observable;
import io.reactivex.disposables.Disposable;
import micdm.transportlive.misc.Clients;
import micdm.transportlive.misc.Cache;

abstract class DefaultStore<Client, Data> {

    @Inject
    Cache cache;

    final Clients<Client> clients = new Clients<>();

    void init() {
        subscribeForData();
    }

    private Disposable subscribeForData() {
        return getStoreRequests().subscribe(this::writeData);
    }

    abstract Observable<Data> getStoreRequests();

    private void writeData(Data data) {
        cache.put(getKey(getEntityId(data)), serialize(data));
    }

    abstract String getEntityId(Data data);

    abstract String getKey(String entityId);

    abstract String serialize(Data data);

    public Observable<Data> getData(String entityId) {
        Data data = readData(entityId);
        return data == null ? Observable.empty() : Observable.just(data);
    }

    private Data readData(String entityId) {
        String data = cache.get(getKey(entityId));
        if (data == null) {
            return null;
        }
        return deserialize(data);
    }

    abstract Data deserialize(String data);

    public void attach(Client client) {
        clients.attach(client);
    }
}
