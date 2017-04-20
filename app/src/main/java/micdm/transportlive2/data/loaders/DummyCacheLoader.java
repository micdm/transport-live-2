package micdm.transportlive2.data.loaders;

import io.reactivex.Maybe;

class DummyCacheLoader<Data> implements BaseLoader.CacheLoader<Data> {

    @Override
    public Maybe<Data> load() {
        return Maybe.empty();
    }
}
