package micdm.transportlive.data.stores;

import android.content.SharedPreferences;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import javax.inject.Inject;

import io.reactivex.Observable;
import io.reactivex.disposables.Disposable;
import micdm.transportlive.misc.Clients;
import micdm.transportlive.misc.Id;
import micdm.transportlive.misc.IdFactory;
import micdm.transportlive.misc.ObservableCache;

public class SelectedRoutesStore {

    public interface Client {

        Observable<Collection<Id>> getSelectRoutesRequests();
    }

    private static final String SHARED_PREFERENCES_KEY = "selectedRoutes";

    @Inject
    IdFactory idFactory;
    @Inject
    ObservableCache observableCache;
    @Inject
    SharedPreferences sharedPreferences;

    private final Clients<Client> clients = new Clients<>();

    void init() {
        subscribeForSelectedRoutes();
    }

    private Disposable subscribeForSelectedRoutes() {
        return clients.get().flatMap(Client::getSelectRoutesRequests).subscribe(this::writeValue);
    }

    public void attach(Client client) {
        clients.attach(client);
    }

    public Observable<Collection<Id>> getSelectedRoutes() {
        return observableCache.get("getSelectedRoutes", () ->
            clients.get()
                .flatMap(Client::getSelectRoutesRequests)
                .startWith(Observable.just(readValue()))
                .replay(1)
                .autoConnect()
        );
    }

    private Collection<Id> readValue() {
        Set<String> ids = sharedPreferences.getStringSet(SHARED_PREFERENCES_KEY, Collections.emptySet());
        Collection<Id> result = new ArrayList<>(ids.size());
        for (String id: ids) {
            result.add(idFactory.newInstance(id));
        }
        return result;
    }

    private void writeValue(Collection<Id> routes) {
        Set<String> ids = new HashSet<>(routes.size());
        for (Id id: routes) {
            ids.add(id.getOriginal());
        }
        sharedPreferences.edit()
            .putStringSet(SHARED_PREFERENCES_KEY, ids)
            .apply();
    }
}
