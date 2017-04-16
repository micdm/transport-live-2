package micdm.transportlive2.misc;

import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class Container<T1> {

    protected interface Factory<T> {

        T newInstance();
    }

    private final Lock lock = new ReentrantLock();

    protected <T2 extends T1> T2 getOrCreateInstance(Map<Id, T2> container, Id key, Factory<T2> factory) {
        lock.lock();
        try {
            T2 instance = container.get(key);
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

    protected void onNewInstance(T1 instance) {}
}
