package micdm.transportlive2.ui.views;

import android.content.Context;
import android.content.res.Resources;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.reactivex.Observable;
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
import micdm.transportlive2.misc.Irrelevant;
import micdm.transportlive2.models.ImmutablePreferences;
import micdm.transportlive2.models.Preferences;
import micdm.transportlive2.models.Route;
import micdm.transportlive2.models.RouteGroup;
import micdm.transportlive2.ui.PreferencesPresenter;
import micdm.transportlive2.ui.Presenters;
import micdm.transportlive2.ui.RoutesPresenter;
import micdm.transportlive2.ui.misc.MiscFunctions;

public class SearchRouteView extends PresentedView implements RoutesPresenter.View, PreferencesPresenter.View {

    static class Adapter extends RecyclerView.Adapter<Adapter.ViewHolder> {

        static class ViewHolder extends RecyclerView.ViewHolder {

            @BindView(R.id.v__search_route__item__name)
            TextView nameView;

            ViewHolder(View itemView) {
                super(itemView);
                ButterKnife.bind(this, itemView);
            }
        }

        private final LayoutInflater layoutInflater;
        private final MiscFunctions miscFunctions;
        private final Resources resources;

        private final Subject<Id> selectRouteRequests = PublishSubject.create();
        private List<RouteInfo> routes = Collections.emptyList();

        Adapter(LayoutInflater layoutInflater, MiscFunctions miscFunctions, Resources resources) {
            this.layoutInflater = layoutInflater;
            this.miscFunctions = miscFunctions;
            this.resources = resources;
            setHasStableIds(true);
        }

        Observable<Id> getSelectRouteRequests() {
            return selectRouteRequests;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            return new ViewHolder(layoutInflater.inflate(R.layout.v__search__route__item, parent, false));
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            RouteInfo info = routes.get(position);
            holder.itemView.setOnClickListener(o -> selectRouteRequests.onNext(info.route.id()));
            holder.nameView.setText(resources.getString(R.string.v__search_route__route, miscFunctions.getRouteGroupName(info.group),
                                                        info.route.number(), info.route.source(), info.route.destination()));
        }

        @Override
        public int getItemCount() {
            return routes.size();
        }

        @Override
        public long getItemId(int position) {
            return routes.get(position).route.id().getNumeric();
        }

        void setRoutes(List<RouteInfo> routes) {
            this.routes = routes;
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

    @BindView(R.id.v__search_route__input)
    ClearableEditText inputView;
    @BindView(R.id.v__search_route__items)
    RecyclerView itemsView;

    public SearchRouteView(Context context, AttributeSet attrs) {
        super(context, attrs);
        if (!isInEditMode()) {
            ComponentHolder.getActivityComponent().inject(this);
        }
    }

    @Override
    void inflateContent(LayoutInflater layoutInflater) {
        layoutInflater.inflate(R.layout.v__search_route, this);
    }

    @Override
    void setupViews() {
        itemsView.setLayoutManager(new LinearLayoutManager(getContext()));
        itemsView.setAdapter(new Adapter(layoutInflater, miscFunctions, resources));
    }

    @Override
    Disposable subscribeForEvents() {
        return new CompositeDisposable(
            subscribeForRoutes(),
            subscribeForSelection()
        );
    }

    private Disposable subscribeForRoutes() {
        return Observable
            .combineLatest(
                presenters.getRoutesPresenter().getResults()
                    .filter(Result::isSuccess)
                    .map(Result::getData),
                presenters.getPreferencesPresenter().getSelectedRoutes(),
                inputView.getText()
                    .map(text -> text.toString().toLowerCase()),
                (groups, routeIds, search) -> {
                    if (search.isEmpty()) {
                        return Collections.<RouteInfo>emptyList();
                    }
                    List<RouteInfo> routes = new ArrayList<>();
                    for (RouteGroup group: groups) {
                        for (Route route: group.routes()) {
                            if ((isRouteGroupMatchesSearch(group, search) || isRouteMatchesSearch(route, search)) && !routeIds.contains(route.id())) {
                                routes.add(new RouteInfo(group, route));
                            }
                        }
                    }
                    return routes;
                }
            )
            .compose(commonFunctions.toMainThread())
            .subscribe(((Adapter) itemsView.getAdapter())::setRoutes);
    }

    private boolean isRouteGroupMatchesSearch(RouteGroup group, CharSequence search) {
        return miscFunctions.getRouteGroupName(group).toString().toLowerCase().contains(search);
    }

    private boolean isRouteMatchesSearch(Route route, CharSequence search) {
        return route.number().contains(search) ||
            route.source().toLowerCase().contains(search) ||
            route.destination().toLowerCase().contains(search);
    }

    private Disposable subscribeForSelection() {
        return ((Adapter) itemsView.getAdapter()).getSelectRouteRequests()
            .subscribe(routeId -> {
                analyticsTracker.trackRouteSelection(routeId.getOriginal());
                inputView.clear();
            });
    }

    @Override
    void attachToPresenters() {
        presenters.getRoutesPresenter().attach(this);
        presenters.getPreferencesPresenter().attach(this);
    }

    @Override
    void detachFromPresenters() {
        presenters.getRoutesPresenter().detach(this);
        presenters.getPreferencesPresenter().detach(this);
    }

    @Override
    public Observable<Object> getLoadRoutesRequests() {
        return Observable.just(Irrelevant.INSTANCE);
    }

    @Override
    public Observable<Preferences> getChangePreferencesRequests() {
        return ((Adapter) itemsView.getAdapter()).getSelectRouteRequests()
            .withLatestFrom(presenters.getPreferencesPresenter().getPreferences(), (routeIds, preferences) ->
                ImmutablePreferences.builder()
                    .from(preferences)
                    .addSelectedRoutes(routeIds)
                    .build()
            );
    }
}
