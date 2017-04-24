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
import io.reactivex.subjects.PublishSubject;
import io.reactivex.subjects.Subject;
import micdm.transportlive2.misc.Irrelevant;
import timber.log.Timber;

abstract class BaseLoader<Data> {

    interface CacheLoader<Data> {

        Maybe<Data> load();
    }

    interface ServerLoader<Data> {

        Single<Data> load();
    }

    interface StoreClient<Data> {

        void setData(Data data);
    }

    @Inject
    @Named("io")
    Scheduler ioScheduler;

    private final Subject<Object> loadRequests = PublishSubject.create();
    private final CacheLoader<Data> cacheLoader;
    private final ServerLoader<Data> serverLoader;
    private final StoreClient<Data> storeClient;

    private final Subject<Result<Data>> results = BehaviorSubject.create();

    BaseLoader(CacheLoader<Data> cacheLoader, ServerLoader<Data> serverLoader, StoreClient<Data> storeClient) {
        this.cacheLoader = cacheLoader;
        this.serverLoader = serverLoader;
        this.storeClient = storeClient;
    }

    void init() {
        subscribeForLoadRequests();
        subscribeForData();
    }

    private Disposable subscribeForLoadRequests() {
        AtomicBoolean isLocked = new AtomicBoolean(false);
        return loadRequests
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

    public Observable<Result<Data>> getData() {
        return results;
    }

    public void load() {
        loadRequests.onNext(Irrelevant.INSTANCE);
    }
}
