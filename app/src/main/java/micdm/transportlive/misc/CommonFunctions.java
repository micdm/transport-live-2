package micdm.transportlive.misc;

import org.javatuples.Pair;

import java.util.ArrayList;
import java.util.Collection;

import javax.inject.Inject;
import javax.inject.Named;

import io.reactivex.Observable;
import io.reactivex.ObservableTransformer;
import io.reactivex.Scheduler;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.BiConsumer;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Predicate;

public class CommonFunctions {

    @Inject
    @Named("mainThread")
    Scheduler mainThreadScheduler;

    CommonFunctions() {

    }

    public <T1, T2> Pair<T1, T2> wrap(T1 first, T2 second) {
        return Pair.with(first, second);
    }

    public <T1, T2> Consumer<Pair<T1, T2>> unwrap(BiConsumer<T1, T2> handler) {
        return pair -> handler.accept(pair.getValue0(), pair.getValue1());
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
}
