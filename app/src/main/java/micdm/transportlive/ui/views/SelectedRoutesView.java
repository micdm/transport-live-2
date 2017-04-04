package micdm.transportlive.ui.views;

import android.animation.Animator;
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

import com.jakewharton.rxbinding2.view.RxView;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.reactivex.Observable;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.subjects.PublishSubject;
import io.reactivex.subjects.Subject;
import micdm.transportlive.ComponentHolder;
import micdm.transportlive.R;
import micdm.transportlive.data.loaders.Result;
import micdm.transportlive.misc.CommonFunctions;
import micdm.transportlive.misc.Irrelevant;
import micdm.transportlive.models.Route;
import micdm.transportlive.models.RouteGroup;
import micdm.transportlive.ui.PresenterStore;
import micdm.transportlive.ui.RoutesPresenter;
import micdm.transportlive.ui.SelectedRoutesPresenter;
import micdm.transportlive.ui.misc.ColorConstructor;

public class SelectedRoutesView extends BaseView implements RoutesPresenter.View, SelectedRoutesPresenter.View {

    private static class RouteInfo {

        final RouteGroup group;
        final Route route;

        private RouteInfo(RouteGroup group, Route route) {
            this.group = group;
            this.route = route;
        }
    }

    static class Adapter extends RecyclerView.Adapter<Adapter.ViewHolder> {

        static class ViewHolder extends RecyclerView.ViewHolder {

            @BindView(R.id.v__selected_routes__item__icon)
            ImageView iconView;
            @BindView(R.id.v__selected_routes__item__number)
            TextView numberView;
//            @BindView(R.id.v__selected_routes__item__stations)
//            TextView stationsView;
            @BindView(R.id.v__selected_routes__item__extra)
            View extraView;
            @BindView(R.id.v__selected_routes__item__remove)
            View removeView;

            ViewHolder(View itemView) {
                super(itemView);
                ButterKnife.bind(this, itemView);
            }
        }

        private final ColorConstructor colorConstructor;
        private final LayoutInflater layoutInflater;
        private final Resources resources;

        private final Subject<Object> toggleRequests = PublishSubject.create();
        private final Subject<String> selectRouteRequests = PublishSubject.create();
        private List<RouteInfo> routes = Collections.emptyList();

        Adapter(ColorConstructor colorConstructor, LayoutInflater layoutInflater, Resources resources) {
            this.colorConstructor = colorConstructor;
            this.layoutInflater = layoutInflater;
            this.resources = resources;
        }

        Observable<Object> getToggleRequests() {
            return toggleRequests;
        }

        Observable<String> getSelectRouteRequests() {
            return selectRouteRequests;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            return new ViewHolder(layoutInflater.inflate(R.layout.v__selected_routes__item, parent, false));
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            RouteInfo info = routes.get(position);
            holder.itemView.setBackgroundColor(colorConstructor.getByString(info.route.id()));
            holder.itemView.setOnClickListener(o -> toggleRequests.onNext(Irrelevant.INSTANCE));
            if (info.group.type() == RouteGroup.Type.TROLLEYBUS) {
                holder.iconView.setImageDrawable(resources.getDrawable(R.drawable.ic_trolleybus));
            }
            if (info.group.type() == RouteGroup.Type.TRAM) {
                holder.iconView.setImageDrawable(resources.getDrawable(R.drawable.ic_tram));
            }
            if (info.group.type() == RouteGroup.Type.BUS) {
                holder.iconView.setImageDrawable(resources.getDrawable(R.drawable.ic_bus));
            }
            holder.numberView.setText(info.route.number());
//            holder.stationsView.setText(String.format("%s\n%s", info.route.source(), info.route.destination()));
            holder.removeView.setOnClickListener(o -> selectRouteRequests.onNext(info.route.id()));
        }

        @Override
        public int getItemCount() {
            return routes.size();
        }

        void setRoutes(List<RouteInfo> routes) {
            this.routes = routes;
            notifyDataSetChanged();
        }
    }

    @Inject
    @Named("showRoutes")
    Animator showRoutesAnimator;
    @Inject
    @Named("hideRoutes")
    Animator hideRoutesAnimator;
    @Inject
    ColorConstructor colorConstructor;
    @Inject
    CommonFunctions commonFunctions;
    @Inject
    LayoutInflater layoutInflater;
    @Inject
    PresenterStore presenterStore;

    @BindView(R.id.v__selected_routes__items)
    RecyclerView itemsView;

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
        itemsView.setLayoutManager(new LinearLayoutManager(getContext()));
        itemsView.setAdapter(new Adapter(colorConstructor, layoutInflater, getResources()));
    }

    @Override
    Disposable subscribeForEvents() {
        return new CompositeDisposable(
            subscribeForSelectedRoutes(),
            subscribeForShowRequests()
        );
    }

    private Disposable subscribeForSelectedRoutes() {
        return Observable
            .combineLatest(
                presenterStore.getRoutesPresenter(this).getResults()
                    .filter(Result::isSuccess)
                    .map(Result::getData),
                presenterStore.getSelectedRoutesPresenter(this).getSelectedRoutes(),
                (groups, routeIds) -> {
                    List<RouteInfo> routes = new ArrayList<>();
                    for (RouteGroup group: groups) {
                        for (Route route: group.routes()) {
                            if (routeIds.contains(route.id())) {
                                routes.add(new RouteInfo(group, route));
                            }
                        }
                    }
                    Collections.sort(routes, (a, b) -> {
                        if (a.group.equals(b.group)) {
                            return a.route.number().compareTo(b.route.number());
                        }
                        return a.group.type().compareTo(b.group.type());
                    });
                    return routes;
                }
            )
            .compose(commonFunctions.toMainThread())
            .subscribe(((Adapter) itemsView.getAdapter())::setRoutes);
    }

    private Disposable subscribeForShowRequests() {
        return ((Adapter) itemsView.getAdapter()).getToggleRequests().subscribe(o -> {
            if (showRoutesAnimator.isRunning() || hideRoutesAnimator.isRunning()) {
                return;
            }
            if (itemsView.getTranslationX() == 0) {
                hideRoutesAnimator.setTarget(itemsView);
                hideRoutesAnimator.start();
            } else {
                showRoutesAnimator.setTarget(itemsView);
                showRoutesAnimator.start();
            }
        });
    }

    @Override
    public Observable<Object> getAttaches() {
        return RxView.attaches(this);
    }

    @Override
    public Observable<Object> getDetaches() {
        return RxView.detaches(this);
    }

    @Override
    public boolean isAttached() {
        return true;
    }

    @Override
    public Observable<Collection<String>> getSelectRoutesRequests() {
        return presenterStore.getSelectedRoutesPresenter(this).getSelectedRoutes().switchMap(routeIds ->
            ((Adapter) itemsView.getAdapter()).getSelectRouteRequests().map(routeId -> {
                Collection<String> result = new HashSet<>(routeIds);
                result.remove(routeId);
                return result;
            })
        );
    }

    @Override
    public Observable<Object> getLoadRoutesRequests() {
        return Observable.just(Irrelevant.INSTANCE);
    }
}
