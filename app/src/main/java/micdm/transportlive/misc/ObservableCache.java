package micdm.transportlive.misc;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import io.reactivex.Observable;

public class ObservableCache {

    public interface Factory<T> {

        Observable<T> newInstance();
    }

    private final Lock lock = new ReentrantLock();
    private final Map<String, Observable<?>> observables = new HashMap<>();

    ObservableCache() {

    }

    public <T> Observable<T> get(String key, Factory<T> factory) {
        lock.lock();
        try {
            Observable<T> result = (Observable<T>) observables.get(key);
            if (result == null) {
                result = factory.newInstance();
                observables.put(key, result);
            }
            return result;
        } finally {
            lock.unlock();
        }
    }
}
