package micdm.transportlive2.data.stores;

import io.reactivex.Maybe;
import io.reactivex.Observable;
import io.reactivex.subjects.PublishSubject;
import io.reactivex.subjects.Subject;
import timber.log.Timber;

public abstract class BaseStore<Data> {

    interface Adapter<Data> {

        String serialize(Data data);
        Data deserialize(String serialized);
    }

    interface Storage {

        String read();
        void write(String value);
    }

    private final Subject<Data> storeRequests = PublishSubject.create();
    private final Adapter<Data> adapter;
    private final Storage storage;
    private final Data initial;

    BaseStore(Adapter<Data> adapter, Storage storage) {
        this(adapter, storage, null);
    }

    BaseStore(Adapter<Data> adapter, Storage storage, Data initial) {
        this.adapter = adapter;
        this.storage = storage;
        this.initial = initial;
    }

    void init() {
        subscribeForWriteRequests();
    }

    private void subscribeForWriteRequests() {
        storeRequests
            .distinctUntilChanged()
            .subscribe(data -> {
                Timber.d("Writing data on %s", this);
                storage.write(adapter.serialize(data));
            });
    }

    public Observable<Data> getData() {
        return storeRequests
            .startWith(getStored().toObservable())
            .distinctUntilChanged();
    }

    public Maybe<Data> getStored() {
        String serialized = storage.read();
        if (serialized != null) {
            try {
                return Maybe.just(adapter.deserialize(serialized));
            } catch (Exception e) {
                Timber.w(e, "Cannot deserialize data");
            }
        }
        return initial == null ? Maybe.empty() : Maybe.just(initial);
    }

    public void store(Data data) {
        storeRequests.onNext(data);
    }
}
