package micdm.transportlive2.ui.views;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.TextView;

import javax.inject.Inject;

import butterknife.BindView;
import io.reactivex.Observable;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import micdm.transportlive2.ComponentHolder;
import micdm.transportlive2.R;
import micdm.transportlive2.data.loaders.Result;
import micdm.transportlive2.misc.CommonFunctions;
import micdm.transportlive2.misc.Id;
import micdm.transportlive2.misc.Irrelevant;
import micdm.transportlive2.models.Station;
import micdm.transportlive2.ui.Presenters;
import micdm.transportlive2.ui.StationPresenter;

public class SelectedStationView extends PresentedView implements StationPresenter.View {

    @Inject
    CommonFunctions commonFunctions;
    @Inject
    Presenters presenters;

    @BindView(R.id.v__selected_station__name)
    TextView nameView;

    private Id stationId;

    public SelectedStationView(Context context, AttributeSet attrs) {
        super(context, attrs);
        if (!isInEditMode()) {
            ComponentHolder.getActivityComponent().inject(this);
        }
    }

    public void setStationId(Id id) {
        stationId = id;
    }

    @Override
    void inflateContent(LayoutInflater layoutInflater) {
        layoutInflater.inflate(R.layout.v__selected_station, this);
    }

    @Override
    void attachToPresenters() {
        presenters.getStationPresenter(stationId).attach(this);
    }

    @Override
    void detachFromPresenters() {
        presenters.getStationPresenter(stationId).detach(this);
    }

    @Override
    Disposable subscribeForEvents() {
        return subscribeForData();
    }

    private Disposable subscribeForData() {
        Observable<Result<Station>> common = presenters.getStationPresenter(stationId).getResults().compose(commonFunctions.toMainThread());
        return new CompositeDisposable(
            common
                .filter(Result::isLoading)
                .subscribe(),
            common
                .filter(Result::isSuccess)
                .map(Result::getData)
                .subscribe(station -> nameView.setText(station.name())),
            common
                .filter(Result::isFail)
                .subscribe()
        );
    }

    @Override
    public Observable<Object> getLoadStationRequests() {
        return Observable.just(Irrelevant.INSTANCE);
    }
}
