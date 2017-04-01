package micdm.transportlive.data.loaders;

import java.util.concurrent.atomic.AtomicBoolean;

import javax.inject.Inject;
import javax.inject.Named;

import io.reactivex.Observable;
import io.reactivex.Scheduler;
import micdm.transportlive.misc.CommonFunctions;
import micdm.transportlive.misc.ObservableCache;
import timber.log.Timber;

abstract class DefaultLoader<Client, Data> extends BaseLoader<Client, Data> {

    @Inject
    CommonFunctions commonFunctions;
    @Inject
    @Named("io")
    Scheduler ioScheduler;
    @Inject
    ObservableCache observableCache;

    DefaultLoader() {

    }

    @Override
    public Observable<Result<Data>> getData() {
        return observableCache.get(this, "getData", () -> {
            AtomicBoolean isLocked = new AtomicBoolean(false);
            return getRequests()
                .filter(requestMode -> !isLocked.get())
                .doOnNext(o -> isLocked.set(true))
                .switchMap(requestMode ->
                    loadFromCache()
                        .map(Result::newSuccess)
                        .switchIfEmpty(
                            loadFromServer()
                                .map(Result::newSuccess)
                                .takeUntil(getCancelRequests())
                                .defaultIfEmpty(Result.newCanceled())
                        )
                        .subscribeOn(ioScheduler)
                        .doOnError(error ->
                            Timber.w(error, "Cannot load data by %s", this)
                        )
                        .onErrorReturn(o -> Result.newFail())
                        .doOnNext(o -> isLocked.set(false))
                        .startWith(Result.newLoading())
                )
                .doOnNext(result ->
                    Timber.d("Loader %s produced result %s", this, result)
                )
                .replay(1)
                .autoConnect();
        });
    }

    private Observable<Object> getRequests() {
        return Observable.merge(
            getLoadRequests(),
            getReloadRequests()
        );
    }

    abstract Observable<Object> getLoadRequests();

    abstract Observable<Object> getReloadRequests();

    abstract Observable<Object> getCancelRequests();

    abstract Observable<Data> loadFromCache();

    abstract Observable<Data> loadFromServer();
}
