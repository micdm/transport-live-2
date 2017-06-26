package micdm.transportlive2.data.stores;

import android.content.res.AssetManager;

import java.io.IOException;
import java.io.InputStream;
import java.util.Scanner;

class AssetStorage implements BaseStore.Storage {

    private final AssetManager assetManager;
    private final String key;

    AssetStorage(AssetManager assetManager, String key) {
        this.assetManager = assetManager;
        this.key = key;
    }

    @Override
    public String read() {
        try {
            InputStream stream = assetManager.open(String.format("%s.json", key));
            Scanner s = new Scanner(stream).useDelimiter("\\A");
            return s.hasNext() ? s.next() : null;
        } catch (IOException e) {
            return null;
        }
    }

    @Override
    public void write(String value) {
        // Nothing to do here
    }
}
