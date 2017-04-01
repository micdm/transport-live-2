package micdm.transportlive.misc;

import java.util.HashMap;
import java.util.Map;

import io.reactivex.Observable;

public class ObservableCache {

    public interface Factory<T> {

        Observable<T> newInstance();
    }

    private final Map<Object, Map<String, Observable<?>>> cache = new HashMap<>();

    ObservableCache() {

    }

    public <T> Observable<T> get(Object owner, String key, Factory<T> factory) {
        Map<String, Observable<?>> observables = cache.get(owner);
        if (observables == null) {
            observables = new HashMap<>();
            cache.put(owner, observables);
        }
        Observable<T> result = (Observable<T>) observables.get(key);
        if (result == null) {
            result = factory.newInstance();
            observables.put(key, result);
        }
        return result;
    }

    public void clear(Object owner) {
        cache.remove(owner);
    }
}
