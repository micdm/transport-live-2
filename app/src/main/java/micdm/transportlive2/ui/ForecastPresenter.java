package micdm.transportlive2.ui;

import javax.inject.Inject;

import io.reactivex.Observable;
import micdm.transportlive2.data.loaders.ForecastLoader;
import micdm.transportlive2.data.loaders.Loaders;
import micdm.transportlive2.data.loaders.Result;
import micdm.transportlive2.misc.CommonFunctions;
import micdm.transportlive2.misc.Id;
import micdm.transportlive2.models.Forecast;

public class ForecastPresenter extends BasePresenter<ForecastPresenter.View> implements ForecastLoader.Client {

    public interface View {

        Observable<Object> getLoadForecastRequests();
    }

    @Inject
    CommonFunctions commonFunctions;
    @Inject
    Loaders loaders;

    private final Id stationId;

    ForecastPresenter(Id stationId) {
        this.stationId = stationId;
    }

    @Override
    void initMore() {
        loaders.getForecastLoader(stationId).attach(this);
    }

    @Override
    public Observable<Object> getLoadForecastRequests() {
        return getViewInput(View::getLoadForecastRequests);
    }

    public Observable<Result<Forecast>> getResults() {
        return loaders.getForecastLoader(stationId).getData();
    }
}
