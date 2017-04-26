package micdm.transportlive2.ui.views;

import android.content.Context;
import android.content.res.Resources;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.subjects.PublishSubject;
import io.reactivex.subjects.Subject;
import micdm.transportlive2.ComponentHolder;
import micdm.transportlive2.R;
import micdm.transportlive2.data.loaders.Result;
import micdm.transportlive2.misc.AnalyticsTracker;
import micdm.transportlive2.misc.CommonFunctions;
import micdm.transportlive2.misc.Id;
import micdm.transportlive2.models.ImmutablePreferences;
import micdm.transportlive2.models.Route;
import micdm.transportlive2.models.RouteGroup;
import micdm.transportlive2.models.RouteInfo;
import micdm.transportlive2.models.Station;
import micdm.transportlive2.ui.misc.MiscFunctions;
import micdm.transportlive2.ui.presenters.Presenters;
import micdm.transportlive2.ui.presenters.SearchPresenter;

public class SearchResultsView extends PresentedView {

    static class Adapter extends RecyclerView.Adapter<Adapter.ViewHolder> {

        static class ViewHolder extends RecyclerView.ViewHolder {

            @BindView(R.id.v__search_results__item__icon)
            ImageView iconView;
            @BindView(R.id.v__search_results__item__name)
            TextView nameView;
            @BindView(R.id.v__search_results__item__description)
            TextView descriptionView;

            ViewHolder(View itemView) {
                super(itemView);
                ButterKnife.bind(this, itemView);
            }
        }

        interface Item {

            Id getId();
            void bindViewHolder(ViewHolder holder);
        }

        static class RouteInfoItem implements Item {

            private final MiscFunctions miscFunctions;
            private final Resources resources;
            private final Subject<Id> selectRequests;
            private final RouteGroup group;
            private final Route route;

            RouteInfoItem(MiscFunctions miscFunctions, Resources resources, Subject<Id> selectRequests, RouteGroup group, Route route) {
                this.miscFunctions = miscFunctions;
                this.resources = resources;
                this.selectRequests = selectRequests;
                this.group = group;
                this.route = route;
            }

            @Override
            public Id getId() {
                return route.id();
            }

            @Override
            public void bindViewHolder(ViewHolder holder) {
                holder.itemView.setOnClickListener(o -> selectRequests.onNext(route.id()));
                holder.iconView.setImageResource(R.drawable.ic_bus);
                holder.nameView.setText(resources.getString(R.string.v__search__item__route_name, miscFunctions.getRouteGroupName(group), route.number()));
                holder.descriptionView.setText(resources.getString(R.string.v__search__item__route_description, route.source(), route.destination()));
            }
        }

        static class StationItem implements Item {

            private final Resources resources;
            private final Subject<Id> selectRequests;
            private final Station station;

            StationItem(Resources resources, Subject<Id> selectRequests, Station station) {
                this.resources = resources;
                this.selectRequests = selectRequests;
                this.station = station;
            }

            @Override
            public Id getId() {
                return station.id();
            }

            @Override
            public void bindViewHolder(ViewHolder holder) {
                holder.itemView.setOnClickListener(o -> selectRequests.onNext(station.id()));
                holder.iconView.setImageResource(R.drawable.ic_station);
                holder.nameView.setText(resources.getString(R.string.v__search__item__station_name, station.name()));
                if (station.description().isEmpty()) {
                    holder.descriptionView.setVisibility(GONE);
                } else {
                    holder.descriptionView.setText(resources.getString(R.string.v__search__item__station_description, station.description()));
                    holder.descriptionView.setVisibility(VISIBLE);
                }
            }
        }

        private final LayoutInflater layoutInflater;

        private List<Item> items = Collections.emptyList();

        Adapter(LayoutInflater layoutInflater) {
            this.layoutInflater = layoutInflater;
            setHasStableIds(true);
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            return new ViewHolder(layoutInflater.inflate(R.layout.v__search_results__item, parent, false));
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            Item item = items.get(position);
            item.bindViewHolder(holder);
        }

        @Override
        public int getItemCount() {
            return items.size();
        }

        @Override
        public long getItemId(int position) {
            return items.get(position).getId().getNumeric();
        }

        void setItems(List<Item> items) {
            this.items = items;
            notifyDataSetChanged();
        }
    }

    @Inject
    AnalyticsTracker analyticsTracker;
    @Inject
    CommonFunctions commonFunctions;
    @Inject
    LayoutInflater layoutInflater;
    @Inject
    MiscFunctions miscFunctions;
    @Inject
    Presenters presenters;
    @Inject
    Resources resources;

    @BindView(R.id.v__search_results__items)
    ProbablyEmptyRecyclerView itemsView;

    private final Subject<Id> selectRoutesRequests = PublishSubject.create();
    private final Subject<Id> selectStationsRequests = PublishSubject.create();

    public SearchResultsView(Context context, AttributeSet attrs) {
        super(context, attrs);
        if (!isInEditMode()) {
            ComponentHolder.getActivityComponent().inject(this);
        }
    }

    @Override
    void inflateContent(LayoutInflater layoutInflater) {
        layoutInflater.inflate(R.layout.v__search_results, this);
    }

    @Override
    void setupViews() {
        itemsView.setLayoutManager(new LinearLayoutManager(getContext()));
        itemsView.setAdapter(new Adapter(layoutInflater));
        itemsView.setItemAnimator(null);
    }

    @Override
    Disposable subscribeForEvents() {
        return new CompositeDisposable(
            subscribeForChangePreferencesRequests(),
            subscribeForSetCurrentStationRequests(),
            subscribeForSearchQuery(),
            subscribeForSearchResults(),
            subscribeForSelection()
        );
    }

    private Disposable subscribeForChangePreferencesRequests() {
        return selectRoutesRequests
            .withLatestFrom(presenters.getPreferencesPresenter().getPreferences(), (routeIds, preferences) ->
                ImmutablePreferences.builder()
                    .from(preferences)
                    .addSelectedRoutes(routeIds)
                    .build()
            )
            .subscribe(presenters.getPreferencesPresenter().viewInput.preferences::set);
    }

    private Disposable subscribeForSetCurrentStationRequests() {
        return selectStationsRequests
            .subscribe(presenters.getCurrentStationPresenter().viewInput.currentStation::set);
    }

    private Disposable subscribeForSearchQuery() {
        return presenters.getSearchPresenter().getSearchQuery()
            .map(query -> query.length() != 0)
            .compose(commonFunctions.toMainThread())
            .subscribe(itemsView::setEmptyCheckEnabled);
    }

    private Disposable subscribeForSearchResults() {
        return presenters.getSearchPresenter().getSearchResults()
            .filter(Result::isSuccess)
            .map(Result::getData)
            .map(this::convertSearchResultsToItems)
            .compose(commonFunctions.toMainThread())
            .subscribe(((Adapter) itemsView.getAdapter())::setItems);
    }

    private List<Adapter.Item> convertSearchResultsToItems(SearchPresenter.SearchResult data) {
        List<Adapter.Item> items = new ArrayList<>(data.routes.size() + data.stations.size());
        for (RouteInfo routeInfo: data.routes) {
            items.add(new Adapter.RouteInfoItem(miscFunctions, resources, selectRoutesRequests, routeInfo.group(), routeInfo.route()));
        }
        for (Station station: data.stations) {
            items.add(new Adapter.StationItem(resources, selectStationsRequests, station));
        }
        return items;
    }

    private Disposable subscribeForSelection() {
        return new CompositeDisposable(
            selectRoutesRequests.subscribe(routeId -> {
                analyticsTracker.trackRouteSelection(routeId.getOriginal());
                presenters.getSearchPresenter().viewInput.reset.call();
            }),
            selectStationsRequests.subscribe(stationId -> {
                analyticsTracker.trackStationSelection(stationId.getOriginal());
                presenters.getSearchPresenter().viewInput.reset.call();
            })
        );
    }
}
