package micdm.transportlive2.ui.presenters;

import javax.inject.Inject;

import io.reactivex.Observable;
import io.reactivex.disposables.Disposable;
import io.reactivex.subjects.BehaviorSubject;
import io.reactivex.subjects.Subject;
import micdm.transportlive2.data.loaders.Loaders;
import micdm.transportlive2.data.loaders.Result;
import micdm.transportlive2.misc.Id;
import micdm.transportlive2.models.Station;
import micdm.transportlive2.ui.misc.properties.NoValueProperty;

public class StationPresenter extends BasePresenter {

    public static class ViewInput {

        public final NoValueProperty loadStation = new NoValueProperty();
    }

    @Inject
    Loaders loaders;

    public final ViewInput viewInput = new ViewInput();
    private final Subject<Result<Station>> results = BehaviorSubject.create();

    private final Id stationId;

    StationPresenter(Id stationId) {
        this.stationId = stationId;
    }

    @Override
    Disposable subscribeForEvents() {
        return subscribeForInput();
    }

    private Disposable subscribeForInput() {
        return viewInput.loadStation.get()
            .switchMap(o -> loaders.getStationLoader(stationId).load())
            .subscribe(results::onNext);
    }

    public Observable<Result<Station>> getResults() {
        return results;
    }
}
