package micdm.transportlive2.misc;

import org.javatuples.Pair;
import org.javatuples.Triplet;

import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;
import javax.inject.Named;

import io.reactivex.Observable;
import io.reactivex.ObservableTransformer;
import io.reactivex.Scheduler;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Predicate;

public class CommonFunctions {

    public interface Handler2<T1, T2> {

        void handle(T1 first, T2 second);
    }

    public interface Handler3<T1, T2, T3> {

        void handle(T1 first, T2 second, T3 third);
    }

    private static final int DEFAULT_MIN_DELAY = 200;

    @Inject
    @Named("mainThread")
    Scheduler mainThreadScheduler;

    CommonFunctions() {}

    public <T1, T2> Pair<T1, T2> wrap(T1 first, T2 second) {
        return Pair.with(first, second);
    }

    public <T1, T2, T3> Triplet<T1, T2, T3> wrap(T1 first, T2 second, T3 third) {
        return Triplet.with(first, second, third);
    }

    public <T1, T2> Consumer<Pair<T1, T2>> unwrap(Handler2<T1, T2> handler) {
        return pair -> handler.handle(pair.getValue0(), pair.getValue1());
    }

    public <T1, T2, T3> Consumer<Triplet<T1, T2, T3>> unwrap(Handler3<T1, T2, T3> handler) {
        return triplet -> handler.handle(triplet.getValue0(), triplet.getValue1(), triplet.getValue2());
    }

    public <T> ObservableTransformer<T, T> getPrevious() {
        return observable -> observable
            .scan(new ArrayList<T>(2), (accumulated, value) -> {
                if (accumulated.isEmpty()) {
                    accumulated.add(0, null);
                    accumulated.add(1, value);
                } else {
                    accumulated.set(0, accumulated.get(1));
                    accumulated.set(1, value);
                }
                return accumulated;
            })
            .skip(2)
            .map(data -> data.get(0));
    }

    public <T> Observable<Collection<T>> getDelta(Observable<Collection<T>> first, Observable<Collection<T>> second) {
        return Observable.zip(
            first,
            second,
            (previous, current) -> {
                Collection<T> added = new ArrayList<>();
                for (T item: current) {
                    if (!previous.contains(item)) {
                        added.add(item);
                    }
                }
                return added;
            }
        );
    }

    public <T> Predicate<T> isEqual(T value) {
        return item -> item.equals(value);
    }

    public <T> ObservableTransformer<Object, T> toConst(T value) {
        return observable -> observable.map(o -> value);
    }

    public ObservableTransformer<Object, Boolean> toTrue() {
        return observable -> observable.map(o -> true);
    }

    public ObservableTransformer<Object, Boolean> toFalse() {
        return observable -> observable.map(o -> false);
    }

    public ObservableTransformer<Object, Object> toNothing() {
        return observable -> observable.map(o -> Irrelevant.INSTANCE);
    }

    public <T> ObservableTransformer<T, T> toMainThread() {
        return observable -> Observable.create(source -> {
            Disposable subscription = observable.observeOn(mainThreadScheduler)
                .subscribe(source::onNext, source::onError, source::onComplete);
            source.setDisposable(subscription);
        });
    }

    public <T> ObservableTransformer<T, T> minDelay() {
        return minDelay(DEFAULT_MIN_DELAY);
    }

    public <T> ObservableTransformer<T, T> minDelay(int delay) {
        return observable -> observable
            .timeInterval()
            .concatMap(timed ->
                timed.time() > delay ?
                    Observable.just(timed.value()) :
                    Observable.just(timed.value()).delay(delay - timed.time(), TimeUnit.MILLISECONDS)
            );
    }
}
