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
import micdm.transportlive2.ComponentHolder;
import micdm.transportlive2.R;
import micdm.transportlive2.data.loaders.Result;
import micdm.transportlive2.misc.CommonFunctions;
import micdm.transportlive2.misc.Irrelevant;
import micdm.transportlive2.models.ImmutableRouteInfo;
import micdm.transportlive2.models.Route;
import micdm.transportlive2.models.RouteGroup;
import micdm.transportlive2.models.RouteInfo;
import micdm.transportlive2.ui.Presenters;

public class SelectedRoutesView extends PresentedView {

    static class Adapter extends RecyclerView.Adapter<Adapter.ViewHolder> {

        static class ViewHolder extends RecyclerView.ViewHolder {

            ViewHolder(View itemView) {
                super(itemView);
            }
        }

        private final LayoutInflater layoutInflater;

        private List<RouteInfo> routes = Collections.emptyList();

        Adapter(LayoutInflater layoutInflater) {
            this.layoutInflater = layoutInflater;
            setHasStableIds(true);
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            return new ViewHolder(layoutInflater.inflate(R.layout.v__selected_routes__item, parent, false));
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            ((SelectedRouteView) holder.itemView).setRouteId(routes.get(position).route().id());
        }

        @Override
        public int getItemCount() {
            return routes.size();
        }

        @Override
        public long getItemId(int position) {
            return routes.get(position).route().id().getNumeric();
        }

        void setRoutes(List<RouteInfo> routes) {
            this.routes = routes;
            notifyDataSetChanged();
        }
    }

    @Inject
    CommonFunctions commonFunctions;
    @Inject
    LayoutInflater layoutInflater;
    @Inject
    Presenters presenters;

    @BindView(R.id.v__selected_routes__items)
    ProbablyEmptyRecyclerView itemsView;

    public SelectedRoutesView(Context context, AttributeSet attrs) {
        super(context, attrs);
        if (!isInEditMode()) {
            ComponentHolder.getActivityComponent().inject(this);
        }
    }

    @Override
    void inflateContent(LayoutInflater layoutInflater) {
        layoutInflater.inflate(R.layout.v__selected_routes, this);
    }

    @Override
    void setupViews() {
        // TODO: анимация? чтоб размер изменялся после удаления элементов
        itemsView.setLayoutManager(new LinearLayoutManager(getContext()));
        itemsView.setAdapter(new Adapter(layoutInflater));
    }

    @Override
    Disposable subscribeForEvents() {
        return new CompositeDisposable(
            subscribeForLoadRoutesRequests(),
            subscribeForSelectedRoutes()
        );
    }

    private Disposable subscribeForLoadRoutesRequests() {
        return Observable.just(Irrelevant.INSTANCE)
            .subscribe(o -> presenters.getRoutesPresenter().viewInput.loadRoutes());
    }

    private Disposable subscribeForSelectedRoutes() {
        return Observable
            .combineLatest(
                presenters.getRoutesPresenter().getResults()
                    .filter(Result::isSuccess)
                    .map(Result::getData),
                presenters.getPreferencesPresenter().getSelectedRoutes(),
                (groups, routeIds) -> {
                    List<RouteInfo> routes = new ArrayList<>();
                    for (RouteGroup group: groups) {
                        for (Route route: group.routes()) {
                            if (routeIds.contains(route.id())) {
                                routes.add(
                                    ImmutableRouteInfo.builder()
                                        .group(group)
                                        .route(route)
                                        .build()
                                );
                            }
                        }
                    }
                    Collections.sort(routes, (a, b) -> {
                        if (a.group().equals(b.group())) {
                            return a.route().number().compareTo(b.route().number());
                        }
                        return a.group().type().compareTo(b.group().type());
                    });
                    return routes;
                }
            )
            .compose(commonFunctions.toMainThread())
            .subscribe(((Adapter) itemsView.getAdapter())::setRoutes);
    }
}
