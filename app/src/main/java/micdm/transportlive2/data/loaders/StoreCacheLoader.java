package micdm.transportlive2.data.loaders;

import io.reactivex.Maybe;
import micdm.transportlive2.data.stores.BaseStore;

class StoreCacheLoader<Data> implements BaseLoader.CacheLoader<Data> {

    private final BaseStore<Data> store;

    StoreCacheLoader(BaseStore<Data> store) {
        this.store = store;
    }

    @Override
    public Maybe<Data> load() {
        return store.getStored();
    }
}
