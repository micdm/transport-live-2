package micdm.transportlive.data.stores;

import javax.inject.Inject;

import io.reactivex.Observable;
import io.reactivex.disposables.Disposable;
import micdm.transportlive.misc.Clients;
import micdm.transportlive.misc.Cache;
import micdm.transportlive.misc.Id;
import timber.log.Timber;

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

    abstract Id getEntityId(Data data);

    abstract String getKey(Id entityId);

    abstract String serialize(Data data);

    public Observable<Data> getData(Id entityId) {
        Data data = readData(entityId);
        return data == null ? Observable.empty() : Observable.just(data);
    }

    private Data readData(Id entityId) {
        String data = cache.get(getKey(entityId));
        if (data == null) {
            return null;
        }
        try {
            return deserialize(data);
        } catch (Exception e) {
            Timber.w(e, "Cannot deserialize data");
            return null;
        }
    }

    abstract Data deserialize(String data);

    public void attach(Client client) {
        clients.attach(client);
    }
}
