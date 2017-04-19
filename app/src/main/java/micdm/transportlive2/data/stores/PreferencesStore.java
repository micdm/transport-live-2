package micdm.transportlive2.data.stores;

import io.reactivex.Observable;
import micdm.transportlive2.models.Preferences;

public class PreferencesStore extends BaseStore<PreferencesStore.Client, Preferences> {

    public interface Client {

        Observable<Preferences> getChangePreferencesRequests();
    }

    PreferencesStore(ClientHandler<Client, Preferences> clientHandler, Adapter<Preferences> adapter, Storage storage, Preferences initial) {
        super(clientHandler, adapter, storage, initial);
    }
}
