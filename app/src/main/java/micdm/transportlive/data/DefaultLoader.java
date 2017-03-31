package micdm.transportlive.data;

import java.util.concurrent.atomic.AtomicBoolean;

import javax.inject.Inject;
import javax.inject.Named;

import io.reactivex.Observable;
import io.reactivex.Scheduler;
import micdm.transportlive.misc.CommonFunctions;
import micdm.transportlive.utils.ObservableCache;
import timber.log.Timber;

abstract class DefaultLoader<Client, Data> extends BaseLoader<Client, Data> {

    enum RequestMode {
        REGULAR,
        FORCED,
    }

    @Inject
    CommonFunctions commonFunctions;
    @Inject
    @Named("io")
    Scheduler ioScheduler;
    @Inject
    ObservableCache observableCache;

    @Override
    public Observable<Result<Data>> getData() {
        return observableCache.get(this, "getData", () -> {
            AtomicBoolean isLocked = new AtomicBoolean(false);
            AtomicBoolean isSuccess = new AtomicBoolean(false);
            return getRequests()
                .filter(requestMode -> !isLocked.get() && (requestMode == RequestMode.FORCED || !isSuccess.get()))
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
                        .doOnNext(o -> isSuccess.set(o.isSuccess()))
                        .startWith(Result.newLoading())
                )
                .replay(1)
                .autoConnect();
        });
    }

    private Observable<RequestMode> getRequests() {
        return Observable.merge(
            getLoadRequests().compose(commonFunctions.toConst(RequestMode.REGULAR)),
            getReloadRequests().compose(commonFunctions.toConst(RequestMode.FORCED))
        );
    }

    abstract Observable<Object> getLoadRequests();

    abstract Observable<Object> getReloadRequests();

    abstract Observable<Object> getCancelRequests();

    abstract Observable<Data> loadFromCache();

    abstract Observable<Data> loadFromServer();
}
