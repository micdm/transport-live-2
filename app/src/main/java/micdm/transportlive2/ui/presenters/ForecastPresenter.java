package micdm.transportlive2.ui.presenters;

import javax.inject.Inject;

import io.reactivex.Observable;
import io.reactivex.disposables.Disposable;
import io.reactivex.subjects.BehaviorSubject;
import io.reactivex.subjects.Subject;
import micdm.transportlive2.data.loaders.Loaders;
import micdm.transportlive2.data.loaders.Result;
import micdm.transportlive2.misc.Id;
import micdm.transportlive2.models.Forecast;
import micdm.transportlive2.ui.misc.properties.NoValueProperty;

public class ForecastPresenter extends BasePresenter {

    public static class ViewInput {

        public final NoValueProperty loadForecast = new NoValueProperty();
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
        return subscribeForInput();
    }

    private Disposable subscribeForInput() {
        return viewInput.loadForecast.get()
            .switchMap(o -> loaders.getForecastLoader(stationId).load())
            .subscribe(results::onNext);
    }

    public Observable<Result<Forecast>> getResults() {
        return results;
    }
}
