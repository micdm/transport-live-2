package micdm.transportlive2.misc;

import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class Container<T> {

    protected interface Factory<T> {

        T newInstance();
    }

    private final Lock lock = new ReentrantLock();

    protected <Item extends T, Key> Item getOrCreateInstance(Map<Key, Item> container, Key key, Factory<Item> factory) {
        lock.lock();
        try {
            Item instance = container.get(key);
            if (instance == null) {
                instance = factory.newInstance();
                onNewInstance(instance);
                container.put(key, instance);
            }
            return instance;
        } finally {
            lock.unlock();
        }
    }

    protected void onNewInstance(T instance) {}
}
