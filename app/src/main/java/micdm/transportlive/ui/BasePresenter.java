package micdm.transportlive.ui;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import io.reactivex.Observable;
import io.reactivex.disposables.Disposable;
import io.reactivex.subjects.BehaviorSubject;

abstract class BasePresenter<T extends BasePresenter.View> {

    interface View {

        Observable<Object> getAttaches();

        Observable<Object> getDetaches();

        boolean isAttached();
    }

    private final BehaviorSubject<Set<T>> views = BehaviorSubject.createDefault(Collections.emptySet());

    private boolean isInitialized;
    private Disposable eventSubscription;

    void init() {
        if (isInitialized) {
            throw new IllegalStateException(String.format("presenter %s already initialized", this));
        }
        eventSubscription = subscribeForEvents();
        isInitialized = true;
    }

    boolean isInitialized() {
        return isInitialized;
    }

    Disposable subscribeForEvents() {
        return null;
    }

    void attachView(T view) {
        Set<T> views = new HashSet<>(this.views.getValue());
        views.add(view);
        this.views.onNext(views);
    }

    void detachView(T view) {
        Set<T> views = new HashSet<>(this.views.getValue());
        views.remove(view);
        this.views.onNext(views);
    }

    boolean hasView(T view) {
        return views.getValue().contains(view);
    }

    Observable<T> getViews() {
        return views.switchMap(Observable::fromIterable);
    }
}
