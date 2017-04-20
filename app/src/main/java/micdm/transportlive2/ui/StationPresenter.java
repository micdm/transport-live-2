package micdm.transportlive2.ui;

import javax.inject.Inject;

import io.reactivex.Observable;
import io.reactivex.disposables.Disposable;
import io.reactivex.subjects.PublishSubject;
import io.reactivex.subjects.Subject;
import micdm.transportlive2.data.loaders.Loaders;
import micdm.transportlive2.data.loaders.Result;
import micdm.transportlive2.data.loaders.StationLoader;
import micdm.transportlive2.misc.Id;
import micdm.transportlive2.models.Station;

public class StationPresenter extends BasePresenter<StationPresenter.View, StationPresenter.ViewInput> implements StationLoader.Client {

    public interface View {

        Observable<Object> getLoadStationRequests();
    }

    static class ViewInput extends BasePresenter.ViewInput<View> {

        private final Subject<Object> loadStationRequests = PublishSubject.create();

        Observable<Object> getLoadStationRequests() {
            return loadStationRequests;
        }

        @Override
        Disposable subscribeForInput(View view) {
            return view.getLoadStationRequests().subscribe(loadStationRequests::onNext);
        }
    }

    @Inject
    Loaders loaders;

    private final Id stationId;

    StationPresenter(Id stationId) {
        super(new ViewInput());
        this.stationId = stationId;
    }

    @Override
    void attachToServices() {
        loaders.getStationLoader(stationId).attach(this);
    }

    @Override
    public Observable<Object> getLoadStationRequests() {
        return viewInput.getLoadStationRequests();
    }

    public Observable<Result<Station>> getResults() {
        return loaders.getStationLoader(stationId).getData();
    }
}
