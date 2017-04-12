package micdm.transportlive2.misc;

import com.jakewharton.disklrucache.DiskLruCache;

import java.io.IOException;

import javax.inject.Inject;

import micdm.transportlive2.App;
import timber.log.Timber;

public class Cache {

    @Inject
    App app;

    private DiskLruCache cache;

    Cache() {}

    void init() {
        try {
            cache = DiskLruCache.open(app.getCacheDir(), 1, 1, Integer.MAX_VALUE);
        } catch (IOException e) {
            Timber.w(e, "Cannot initialize cache");
        }
    }

    public void put(String key, String value) {
        if (cache == null) {
            return;
        }
        try {
            DiskLruCache.Editor editor = cache.edit(key);
            editor.set(0, value);
            editor.commit();
        } catch (IOException e) {
            Timber.w(e, "Cannot write into cache by key %s", key);
        }
    }

    public String get(String key) {
        if (cache == null) {
            return null;
        }
        try {
            DiskLruCache.Snapshot snapshot = cache.get(key);
            if (snapshot == null) {
                return null;
            }
            return snapshot.getString(0);
        } catch (IOException e) {
            Timber.w(e, "Cannot read from cache by key %s", key);
            return null;
        }
    }
}
