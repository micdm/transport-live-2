package micdm.transportlive2.ui.views;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.jakewharton.rxbinding2.view.RxView;

import java.util.Collection;
import java.util.HashSet;

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
import micdm.transportlive2.models.ImmutablePreferences;
import micdm.transportlive2.models.Station;
import micdm.transportlive2.ui.presenters.Presenters;

public class SelectedStationView extends PresentedView {

    @Inject
    CommonFunctions commonFunctions;
    @Inject
    Presenters presenters;

    @BindView(R.id.v__selected_station__name)
    TextView nameView;
    @BindView(R.id.v__selected_station__description)
    TextView descriptionView;
    @BindView(R.id.v__selected_station__favorite)
    View favoriteView;

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
    Disposable subscribeForEvents() {
        return new CompositeDisposable(
            subscribeForLoadStationRequests(),
            subscribeForChangePreferencesRequests(),
            subscribeForData()
        );
    }

    private Disposable subscribeForLoadStationRequests() {
        return Observable.just(Irrelevant.INSTANCE)
            .subscribe(o -> presenters.getStationPresenter(stationId).viewInput.loadStation());
    }

    private Disposable subscribeForChangePreferencesRequests() {
        return RxView.clicks(favoriteView)
            .map(o -> stationId)
            .withLatestFrom(presenters.getPreferencesPresenter().getPreferences(), (stationId, preferences) -> {
                Collection<Id> selectedStations = new HashSet<>(preferences.selectedStations());
                selectedStations.remove(stationId);
                return ImmutablePreferences.builder()
                    .from(preferences)
                    .selectedStations(selectedStations)
                    .build();
            })
            .subscribe(presenters.getPreferencesPresenter().viewInput::changePreferences);
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
                .subscribe(station -> {
                    nameView.setText(station.name());
                    if (station.description().isEmpty()) {
                        descriptionView.setVisibility(GONE);
                    } else {
                        descriptionView.setText(station.description());
                        descriptionView.setVisibility(VISIBLE);
                    }
                }),
            common
                .filter(Result::isFail)
                .subscribe()
        );
    }
}
