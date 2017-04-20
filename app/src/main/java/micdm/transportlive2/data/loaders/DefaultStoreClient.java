package micdm.transportlive2.data.loaders;

import io.reactivex.subjects.BehaviorSubject;
import io.reactivex.subjects.Subject;
import micdm.transportlive2.data.stores.BaseStore;

abstract class DefaultStoreClient<Client, Data> implements BaseLoader.StoreClient<Data> {

    final BaseStore<Client, Data> store;
    final Subject<Data> data = BehaviorSubject.create();

    DefaultStoreClient(BaseStore<Client, Data> store) {
        this.store = store;
    }

    @Override
    public void setData(Data data) {
        this.data.onNext(data);
    }
}
