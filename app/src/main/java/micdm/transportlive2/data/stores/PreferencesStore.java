package micdm.transportlive2.data.stores;

import micdm.transportlive2.models.Preferences;

public class PreferencesStore extends BaseStore<Preferences> {

    PreferencesStore(Adapter<Preferences> adapter, Storage storage, Preferences initial) {
        super(adapter, storage, initial);
    }
}
