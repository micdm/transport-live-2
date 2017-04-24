package micdm.transportlive2.data.loaders;

import micdm.transportlive2.data.stores.BaseStore;

abstract class DefaultStoreClient<Data> implements BaseLoader.StoreClient<Data> {

    final BaseStore<Data> store;

    DefaultStoreClient(BaseStore<Data> store) {
        this.store = store;
    }

    @Override
    public void setData(Data data) {
        store.store(data);
    }
}
