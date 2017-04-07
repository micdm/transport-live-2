package micdm.transportlive2.data.loaders;

import java.util.concurrent.atomic.AtomicBoolean;

import javax.inject.Inject;
import javax.inject.Named;

import io.reactivex.Observable;
import io.reactivex.Scheduler;
import micdm.transportlive2.misc.CommonFunctions;
import micdm.transportlive2.misc.ObservableCache;
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
        return observableCache.get("getData", () -> {
            AtomicBoolean isLocked = new AtomicBoolean(false);
            return getLoadRequests()
                .filter(o -> !isLocked.get())
                .doOnNext(o -> isLocked.set(true))
                .switchMap(o ->
                    loadFromCache()
                        .map(Result::newSuccess)
                        .switchIfEmpty(
                            loadFromServer().map(Result::newSuccess)
                        )
                        .subscribeOn(ioScheduler)
                        .doOnError(error ->
                            Timber.w(error, "Cannot load data by %s", this)
                        )
                        .onErrorReturn(error -> Result.newFail())
                        .doOnNext(result -> isLocked.set(false))
                        .startWith(Result.newLoading())
                )
                .doOnNext(result ->
                    Timber.d("Loader %s produced result %s", this, result)
                )
                .replay(1)
                .refCount();
        });
    }

    abstract Observable<Object> getLoadRequests();

    abstract Observable<Data> loadFromCache();

    abstract Observable<Data> loadFromServer();
}
