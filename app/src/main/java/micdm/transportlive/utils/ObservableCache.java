package micdm.transportlive.utils;

import java.util.HashMap;
import java.util.Map;

import io.reactivex.Observable;

public class ObservableCache {

    private final Map<Object, Map<String, Observable<?>>> cache = new HashMap<>();

    public <T> Observable<T> get(Object owner, String key, Observable<T> defaultValue) {
        Map<String, Observable<?>> observables = cache.get(owner);
        if (observables == null) {
            observables = new HashMap<>();
            cache.put(owner, observables);
        }
        Observable<T> result = (Observable<T>) observables.get(key);
        if (result == null) {
            result = defaultValue;
            observables.put(key, defaultValue);
        }
        return result;
    }

    public void clear(Object owner) {
        cache.remove(owner);
    }
}
