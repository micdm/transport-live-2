package micdm.transportlive.ui;

import javax.inject.Inject;
import javax.inject.Named;

import io.reactivex.Observable;
import io.reactivex.Scheduler;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Function;
import micdm.transportlive.misc.Clients;

abstract class BasePresenter<T extends BasePresenter.View> {

    interface View {

        Observable<Object> getAttaches();
        Observable<Object> getDetaches();
        boolean isAttached();
    }

    @Inject
    @Named("mainThread")
    Scheduler mainThreadScheduler;

    private final Clients<T> clients = new Clients<>();

    private boolean isInitialized;

    void init() {
        if (isInitialized) {
            throw new IllegalStateException(String.format("presenter %s already initialized", this));
        }
        initMore();
        subscribeForEvents();
        isInitialized = true;
    }

    boolean isInitialized() {
        return isInitialized;
    }

    void initMore() {

    }

    Disposable subscribeForEvents() {
        return null;
    }

    void attachView(T view) {
        clients.attach(view);
    }

    void detachView(T view) {
        clients.detach(view);
    }

    boolean hasView(T view) {
        return clients.has(view);
    }

    <R> Observable<R> getViewInput(Function<T, Observable<R>> callback) {
        return clients.get()
            .observeOn(mainThreadScheduler)
            .flatMap(callback);
    }
}
