package micdm.transportlive2.ui.presenters;

import java.util.Collection;

import javax.inject.Inject;

import io.reactivex.Observable;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.subjects.BehaviorSubject;
import io.reactivex.subjects.Subject;
import micdm.transportlive2.data.stores.Stores;
import micdm.transportlive2.misc.Id;
import micdm.transportlive2.models.Preferences;
import micdm.transportlive2.ui.misc.properties.ValueProperty;

public class PreferencesPresenter extends BasePresenter {

    public static class ViewInput {

        public final ValueProperty<Preferences> preferences = new ValueProperty<>();
    }

    @Inject
    Stores stores;

    public final ViewInput viewInput = new ViewInput();
    private final Subject<Preferences> preferences = BehaviorSubject.create();

    @Override
    Disposable subscribeForEvents() {
        return new CompositeDisposable(
            subscribeForInput(),
            subscribeForPreferences()
        );
    }

    private Disposable subscribeForInput() {
        return viewInput.preferences.get().subscribe(stores.getPreferencesStore()::store);
    }

    private Disposable subscribeForPreferences() {
        return stores.getPreferencesStore().getData().subscribe(preferences::onNext);
    }

    public Observable<Preferences> getPreferences() {
        return preferences;
    }

    public Observable<Collection<Id>> getSelectedRoutes() {
        return preferences
            .<Collection<Id>>map(Preferences::selectedRoutes)
            .distinctUntilChanged();
    }

    public Observable<Collection<Id>> getSelectedStations() {
        return preferences
            .<Collection<Id>>map(Preferences::selectedStations)
            .distinctUntilChanged();
    }

    public Observable<Boolean> getNeedUseHdMap() {
        return preferences
            .map(Preferences::needUseHdMap)
            .distinctUntilChanged();
    }

    public Observable<Boolean> getNeedShowStations() {
        return preferences
            .map(Preferences::needShowStations)
            .distinctUntilChanged();
    }

    public Observable<Preferences.CameraPosition> getCameraPosition() {
        return preferences
            .map(Preferences::cameraPosition)
            .distinctUntilChanged();
    }
}
