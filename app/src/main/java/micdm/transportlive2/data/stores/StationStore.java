package micdm.transportlive2.data.stores;

import micdm.transportlive2.models.Station;

public class StationStore extends BaseStore<Station> {

    StationStore(Adapter<Station> adapter, Storage storage) {
        super(adapter, storage);
    }
}
