package micdm.transportlive2.data.loaders;

import io.reactivex.Maybe;
import micdm.transportlive2.data.stores.BaseStore;

public class DefaultCacheClient<Data> implements BaseLoader.CacheClient<Data> {

    private final BaseStore<Data> store;

    DefaultCacheClient(BaseStore<Data> store) {
        this.store = store;
    }

    @Override
    public Maybe<Data> load() {
        return store.getStored();
    }

    @Override
    public void store(Data data) {
        store.store(data);
    }
}
