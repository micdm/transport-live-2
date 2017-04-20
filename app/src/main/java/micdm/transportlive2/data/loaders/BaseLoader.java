package micdm.transportlive2.data.loaders;

import java.util.concurrent.atomic.AtomicBoolean;

import javax.inject.Inject;
import javax.inject.Named;

import io.reactivex.Maybe;
import io.reactivex.Observable;
import io.reactivex.Scheduler;
import io.reactivex.Single;
import io.reactivex.disposables.Disposable;
import io.reactivex.subjects.BehaviorSubject;
import io.reactivex.subjects.Subject;
import micdm.transportlive2.misc.Clients;
import timber.log.Timber;

abstract class BaseLoader<Client, Data> {

    interface ClientHandler<Client> {

        Observable<Object> getLoadRequests(Clients<Client> clients);
    }

    interface CacheLoader<Data> {

        Maybe<Data> load();
    }

    interface ServerLoader<Data> {

        Single<Data> load();
    }

    interface StoreClient<Data> {

        void attach();
        void setData(Data data);
    }

    @Inject
    @Named("io")
    Scheduler ioScheduler;

    private final Clients<Client> clients = new Clients<>();

    private final ClientHandler<Client> clientHandler;
    private final CacheLoader<Data> cacheLoader;
    private final ServerLoader<Data> serverLoader;
    private final StoreClient<Data> storeClient;

    private final Subject<Result<Data>> results = BehaviorSubject.create();

    BaseLoader(ClientHandler<Client> clientHandler, CacheLoader<Data> cacheLoader, ServerLoader<Data> serverLoader, StoreClient<Data> storeClient) {
        this.clientHandler = clientHandler;
        this.cacheLoader = cacheLoader;
        this.serverLoader = serverLoader;
        this.storeClient = storeClient;
    }

    void init() {
        subscribeForLoadRequests();
        subscribeForData();
        storeClient.attach();
    }

    private Disposable subscribeForLoadRequests() {
        AtomicBoolean isLocked = new AtomicBoolean(false);
        return clientHandler.getLoadRequests(clients)
            .filter(o -> !isLocked.get())
            .doOnNext(o -> isLocked.set(true))
            .switchMap(o ->
                cacheLoader.load()
                    .toObservable()
                    .switchIfEmpty(
                        serverLoader.load().toObservable()
                    )
                    .map(Result::newSuccess)
                    .subscribeOn(ioScheduler)
                    .doOnError(error ->
                        Timber.w(error, "Cannot load data by %s", this)
                    )
                    .onErrorReturn(error -> Result.newFail())
                    .doOnNext(result -> isLocked.set(false))
                    .startWith(Result.newLoading())
            )
            .doOnNext(result ->
                Timber.v("Loader %s produced result %s", this, result)
            )
            .subscribe(results::onNext);
    }

    private Disposable subscribeForData() {
        return results
            .filter(Result::isSuccess)
            .map(Result::getData)
            .subscribe(storeClient::setData);
    }

    public void attach(Client client) {
        clients.attach(client);
    }

    public void detach(Client client) {
        clients.detach(client);
    }

    public Observable<Result<Data>> getData() {
        return results;
    }
}
