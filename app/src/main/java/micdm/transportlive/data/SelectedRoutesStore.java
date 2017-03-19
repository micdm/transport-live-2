package micdm.transportlive.data;

import android.content.SharedPreferences;

import java.util.Set;

import javax.inject.Inject;

import io.reactivex.disposables.Disposable;

public class SelectedRoutesStore {

    private static final String SHARED_PREFERENCES_KEY = "selectedRoutes";

    @Inject
    DataRepository dataRepository;
    @Inject
    SharedPreferences sharedPreferences;

    void init() {
        subscribeForSelectedRoutes();
        if (hasValue()) {
            dataRepository.putSelectedRoutes(readValue());
        }
    }

    private Disposable subscribeForSelectedRoutes() {
        return dataRepository.getSelectedRoutes().subscribe(this::writeValue);
    }

    private boolean hasValue() {
        return sharedPreferences.contains(SHARED_PREFERENCES_KEY);
    }

    private Set<String> readValue() {
        return sharedPreferences.getStringSet(SHARED_PREFERENCES_KEY, null);
    }

    private void writeValue(Set<String> routes) {
        sharedPreferences.edit()
            .putStringSet(SHARED_PREFERENCES_KEY, routes)
            .apply();
    }
}
