package micdm.transportlive2.data.loaders;

import io.reactivex.Maybe;

class DummyCacheClient<Data> implements BaseLoader.CacheClient<Data> {

    @Override
    public Maybe<Data> load() {
        return Maybe.empty();
    }

    @Override
    public void store(Data data) {}
}
