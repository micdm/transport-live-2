package micdm.transportlive2.ui;

import java.util.Collection;

import javax.inject.Inject;

import io.reactivex.Observable;
import io.reactivex.disposables.Disposable;
import io.reactivex.subjects.BehaviorSubject;
import io.reactivex.subjects.PublishSubject;
import io.reactivex.subjects.Subject;
import micdm.transportlive2.data.stores.PreferencesStore;
import micdm.transportlive2.data.stores.Stores;
import micdm.transportlive2.misc.Id;
import micdm.transportlive2.models.Preferences;

public class PreferencesPresenter extends BasePresenter<PreferencesPresenter.View, PreferencesPresenter.ViewInput> implements PreferencesStore.Client {

    public interface View {

        Observable<Preferences> getChangePreferencesRequests();
    }

    static class ViewInput extends BasePresenter.ViewInput<View> {

        private final Subject<Preferences> changePreferencesRequests = PublishSubject.create();

        Observable<Preferences> getChangePreferencesRequests() {
            return changePreferencesRequests;
        }

        @Override
        Disposable subscribeForInput(View view) {
            return view.getChangePreferencesRequests().subscribe(changePreferencesRequests::onNext);
        }
    }

    @Inject
    Stores stores;

    private final Subject<Preferences> preferences = BehaviorSubject.create();

    PreferencesPresenter() {
        super(new ViewInput());
    }

    @Override
    Disposable subscribeForEvents() {
        return subscribeForPreferences();
    }

    private Disposable subscribeForPreferences() {
        return stores.getPreferencesStore().getData().subscribe(preferences::onNext);
    }

    @Override
    void attachToServices() {
        stores.getPreferencesStore().attach(this);
    }

    @Override
    public Observable<Preferences> getChangePreferencesRequests() {
        return viewInput.getChangePreferencesRequests();
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
