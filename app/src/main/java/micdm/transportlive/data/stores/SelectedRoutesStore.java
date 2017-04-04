package micdm.transportlive.data.stores;

import android.content.SharedPreferences;

import java.util.Collection;
import java.util.HashSet;

import javax.inject.Inject;

import io.reactivex.Observable;
import io.reactivex.disposables.Disposable;
import micdm.transportlive.misc.Clients;
import micdm.transportlive.misc.ObservableCache;

public class SelectedRoutesStore {

    public interface Client {

        Observable<Collection<String>> getSelectRoutesRequests();
    }

    private static final String SHARED_PREFERENCES_KEY = "selectedRoutes";

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

    public Observable<Collection<String>> getSelectedRoutes() {
        return observableCache.get("getSelectedRoutes", () ->
            clients.get()
                .flatMap(Client::getSelectRoutesRequests)
                .startWith(hasValue() ? Observable.just(readValue()) : Observable.empty())
                .replay(1)
                .autoConnect()
        );
    }

    private boolean hasValue() {
        return sharedPreferences.contains(SHARED_PREFERENCES_KEY);
    }

    private Collection<String> readValue() {
        return sharedPreferences.getStringSet(SHARED_PREFERENCES_KEY, null);
    }

    private void writeValue(Collection<String> routes) {
        sharedPreferences.edit()
            .putStringSet(SHARED_PREFERENCES_KEY, new HashSet<>(routes))
            .apply();
    }
}
