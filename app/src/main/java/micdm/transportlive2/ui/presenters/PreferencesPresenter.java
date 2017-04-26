package micdm.transportlive2.ui.presenters;

import java.util.Collection;

import javax.inject.Inject;

import io.reactivex.Observable;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.subjects.BehaviorSubject;
import io.reactivex.subjects.PublishSubject;
import io.reactivex.subjects.Subject;
import micdm.transportlive2.data.stores.Stores;
import micdm.transportlive2.misc.Id;
import micdm.transportlive2.models.Preferences;

public class PreferencesPresenter extends BasePresenter {

    public static class ViewInput {

        private final Subject<Preferences> changePreferencesRequests = PublishSubject.create();

        Observable<Preferences> getChangePreferencesRequests() {
            return changePreferencesRequests;
        }

        public void changePreferences(Preferences preferences) {
            changePreferencesRequests.onNext(preferences);
        }
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
        return viewInput.getChangePreferencesRequests().subscribe(stores.getPreferencesStore()::store);
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
