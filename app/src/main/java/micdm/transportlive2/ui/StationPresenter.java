package micdm.transportlive2.ui;

import javax.inject.Inject;

import io.reactivex.Observable;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.subjects.BehaviorSubject;
import io.reactivex.subjects.PublishSubject;
import io.reactivex.subjects.Subject;
import micdm.transportlive2.data.loaders.Loaders;
import micdm.transportlive2.data.loaders.Result;
import micdm.transportlive2.misc.Id;
import micdm.transportlive2.misc.Irrelevant;
import micdm.transportlive2.models.Station;

public class StationPresenter extends BasePresenter {

    public static class ViewInput {

        private final Subject<Object> loadStationRequests = PublishSubject.create();

        Observable<Object> getLoadStationRequests() {
            return loadStationRequests;
        }

        public void loadStation() {
            loadStationRequests.onNext(Irrelevant.INSTANCE);
        }
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
        return new CompositeDisposable(
            subscribeForInput(),
            subscribeForResults()
        );
    }

    private Disposable subscribeForInput() {
        return viewInput.getLoadStationRequests().subscribe(o -> loaders.getStationLoader(stationId).load());
    }

    private Disposable subscribeForResults() {
        return loaders.getStationLoader(stationId).getData().subscribe(results::onNext);
    }

    public Observable<Result<Station>> getResults() {
        return results;
    }
}
