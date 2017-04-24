package micdm.transportlive2.data.stores;

import micdm.transportlive2.models.Path;

public class PathStore extends BaseStore<Path> {

    PathStore(Adapter<Path> adapter, Storage storage) {
        super(adapter, storage);
    }
}
