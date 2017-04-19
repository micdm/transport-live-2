package micdm.transportlive2.data.stores;

import micdm.transportlive2.misc.Cache;

class CacheStorage implements BaseStore.Storage {

    private final Cache cache;
    private final String key;

    CacheStorage(Cache cache, String key) {
        this.cache = cache;
        this.key = key;
    }

    @Override
    public String read() {
        return cache.get(key);
    }

    @Override
    public void write(String value) {
        cache.put(key, value);
    }
}
