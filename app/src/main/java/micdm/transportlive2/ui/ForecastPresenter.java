package micdm.transportlive2.ui;

import javax.inject.Inject;

import io.reactivex.Observable;
import io.reactivex.disposables.Disposable;
import io.reactivex.subjects.PublishSubject;
import io.reactivex.subjects.Subject;
import micdm.transportlive2.data.loaders.ForecastLoader;
import micdm.transportlive2.data.loaders.Loaders;
import micdm.transportlive2.data.loaders.Result;
import micdm.transportlive2.misc.Id;
import micdm.transportlive2.models.Forecast;

public class ForecastPresenter extends BasePresenter<ForecastPresenter.View, ForecastPresenter.ViewInput> implements ForecastLoader.Client {

    public interface View {

        Observable<Object> getLoadForecastRequests();
    }

    static class ViewInput extends BasePresenter.ViewInput<View> {

        private final Subject<Object> loadForecastRequests = PublishSubject.create();

        Observable<Object> getLoadForecastRequests() {
            return loadForecastRequests;
        }

        @Override
        Disposable subscribeForInput(View view) {
            return view.getLoadForecastRequests().subscribe(loadForecastRequests::onNext);
        }
    }

    @Inject
    Loaders loaders;

    private final Id stationId;

    ForecastPresenter(Id stationId) {
        super(new ViewInput());
        this.stationId = stationId;
    }

    @Override
    void attachToServices() {
        loaders.getForecastLoader(stationId).attach(this);
    }

    @Override
    public Observable<Object> getLoadForecastRequests() {
        return viewInput.getLoadForecastRequests();
    }

    public Observable<Result<Forecast>> getResults() {
        return loaders.getForecastLoader(stationId).getData();
    }
}
