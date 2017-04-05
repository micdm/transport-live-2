package micdm.transportlive.ui;

import javax.inject.Inject;
import javax.inject.Named;

import io.reactivex.Observable;
import io.reactivex.Scheduler;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Function;
import micdm.transportlive.misc.Clients;

abstract class BasePresenter<T> {

    @Inject
    @Named("mainThread")
    Scheduler mainThreadScheduler;

    private final Clients<T> clients = new Clients<>();

    void init() {
        subscribeForEvents();
        initMore();
    }

    void initMore() {

    }

    Disposable subscribeForEvents() {
        return null;
    }

    public void attach(T view) {
        clients.attach(view);
    }

    public void detach(T view) {
        clients.detach(view);
    }

    <R> Observable<R> getViewInput(Function<T, Observable<R>> callback) {
        return clients.get()
            .observeOn(mainThreadScheduler)
            .flatMap(callback);
    }
}
