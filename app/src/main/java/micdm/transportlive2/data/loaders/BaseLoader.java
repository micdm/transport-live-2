package micdm.transportlive2.data.loaders;

import javax.inject.Inject;
import javax.inject.Named;

import io.reactivex.Maybe;
import io.reactivex.Observable;
import io.reactivex.Scheduler;
import io.reactivex.Single;
import timber.log.Timber;

abstract class BaseLoader<Data> {

    interface CacheClient<Data> {

        Maybe<Data> load();
        void store(Data data);
    }

    interface ServerLoader<Data> {

        Single<Data> load();
    }

    @Inject
    @Named("io")
    Scheduler ioScheduler;

    private final CacheClient<Data> cacheClient;
    private final ServerLoader<Data> serverLoader;

    BaseLoader(CacheClient<Data> cacheClient, ServerLoader<Data> serverLoader) {
        this.cacheClient = cacheClient;
        this.serverLoader = serverLoader;
    }

    public Observable<Result<Data>> load() {
        return cacheClient.load()
            .toObservable()
            .switchIfEmpty(
                serverLoader.load()
                    .toObservable()
                    .doOnNext(cacheClient::store)
            )
            .map(Result::newSuccess)
            .doOnError(error ->
                Timber.w(error, "Cannot load data by %s", this)
            )
            .onErrorReturn(error -> Result.newFail())
            .startWith(Result.newLoading())
            .doOnNext(result ->
                Timber.v("Loader %s produced result %s", this, result)
            )
            .subscribeOn(ioScheduler);
    }
}
