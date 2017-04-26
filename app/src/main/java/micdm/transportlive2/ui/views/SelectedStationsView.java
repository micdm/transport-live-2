package micdm.transportlive2.ui.views;

import android.content.Context;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.inject.Inject;

import butterknife.BindView;
import io.reactivex.Observable;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.subjects.PublishSubject;
import io.reactivex.subjects.Subject;
import micdm.transportlive2.ComponentHolder;
import micdm.transportlive2.R;
import micdm.transportlive2.misc.Id;
import micdm.transportlive2.ui.presenters.Presenters;

public class SelectedStationsView extends BaseView {

    static class Adapter extends RecyclerView.Adapter<Adapter.ViewHolder> {

        static class ViewHolder extends RecyclerView.ViewHolder {

            ViewHolder(View itemView) {
                super(itemView);
            }
        }

        private final LayoutInflater layoutInflater;

        private final Subject<Id> selectStationsRequests = PublishSubject.create();
        private List<Id> stationIds = Collections.emptyList();

        Adapter(LayoutInflater layoutInflater) {
            this.layoutInflater = layoutInflater;
            setHasStableIds(true);
        }

        Observable<Id> getSelectStationsRequests() {
            return selectStationsRequests;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            return new ViewHolder(layoutInflater.inflate(R.layout.v__selected_stations__item, parent, false));
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            Id stationId = stationIds.get(position);
            holder.itemView.setOnClickListener(o -> selectStationsRequests.onNext(stationId));
            ((SelectedStationView) holder.itemView).setStationId(stationId);
        }

        @Override
        public int getItemCount() {
            return stationIds.size();
        }

        @Override
        public long getItemId(int position) {
            return stationIds.get(position).getNumeric();
        }

        void setStationIds(List<Id> ids) {
            stationIds = ids;
            notifyDataSetChanged();
        }
    }

    @Inject
    Context context;
    @Inject
    LayoutInflater layoutInflater;
    @Inject
    Presenters presenters;

    @BindView(R.id.v__selected_stations__items)
    ProbablyEmptyRecyclerView stationsView;

    public SelectedStationsView(Context context, AttributeSet attrs) {
        super(context, attrs);
        if (!isInEditMode()) {
            ComponentHolder.getActivityComponent().inject(this);
        }
    }

    @Override
    void inflateContent(LayoutInflater layoutInflater) {
        layoutInflater.inflate(R.layout.v__selected_stations, this);
    }

    @Override
    void setupViews() {
        stationsView.setLayoutManager(new LinearLayoutManager(context));
        stationsView.setAdapter(new Adapter(layoutInflater));
    }

    @Override
    Disposable subscribeForEvents() {
        return new CompositeDisposable(
            subscribeForSetCurrentStationRequests(),
            subscribeForSelectedStations()
        );
    }

    private Disposable subscribeForSetCurrentStationRequests() {
        return ((Adapter) stationsView.getAdapter()).getSelectStationsRequests()
            .subscribe(presenters.getCurrentStationPresenter().viewInput::setCurrentStation);
    }

    private Disposable subscribeForSelectedStations() {
        return presenters.getPreferencesPresenter().getSelectedStations()
            .map(ids -> {
                List<Id> result = new ArrayList<>(ids);
                Collections.sort(result, (a, b) -> a.getOriginal().compareTo(b.getOriginal()));
                return result;
            })
            .subscribe(((Adapter) stationsView.getAdapter())::setStationIds);
    }
}
