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
import micdm.transportlive2.models.Forecast;

public class ForecastPresenter extends BasePresenter {

    public static class ViewInput {

        private final Subject<Object> loadForecastRequests = PublishSubject.create();

        Observable<Object> getLoadForecastRequests() {
            return loadForecastRequests;
        }

        public void loadForecast() {
            loadForecastRequests.onNext(Irrelevant.INSTANCE);
        }
    }

    @Inject
    Loaders loaders;

    public final ViewInput viewInput = new ViewInput();
    private final Subject<Result<Forecast>> results = BehaviorSubject.create();

    private final Id stationId;

    ForecastPresenter(Id stationId) {
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
        return viewInput.getLoadForecastRequests().subscribe(o -> loaders.getForecastLoader(stationId).load());
    }

    private Disposable subscribeForResults() {
        return loaders.getForecastLoader(stationId).getData().subscribe(results::onNext);
    }

    public Observable<Result<Forecast>> getResults() {
        return results;
    }
}
