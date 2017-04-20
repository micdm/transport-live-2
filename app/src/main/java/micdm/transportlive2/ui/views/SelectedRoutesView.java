package micdm.transportlive2.ui.views;

import android.animation.Animator;
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
import javax.inject.Named;

import butterknife.BindView;
import io.reactivex.Observable;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.subjects.PublishSubject;
import io.reactivex.subjects.Subject;
import micdm.transportlive2.ComponentHolder;
import micdm.transportlive2.R;
import micdm.transportlive2.data.loaders.Result;
import micdm.transportlive2.misc.CommonFunctions;
import micdm.transportlive2.misc.Irrelevant;
import micdm.transportlive2.models.Route;
import micdm.transportlive2.models.RouteGroup;
import micdm.transportlive2.ui.Presenters;
import micdm.transportlive2.ui.RoutesPresenter;

public class SelectedRoutesView extends PresentedView implements RoutesPresenter.View {

    static class Adapter extends RecyclerView.Adapter<Adapter.ViewHolder> {

        static class ViewHolder extends RecyclerView.ViewHolder {

            ViewHolder(View itemView) {
                super(itemView);
            }
        }

        private final LayoutInflater layoutInflater;

        private final Subject<Object> toggleRequests = PublishSubject.create();
        private List<RouteInfo> routes = Collections.emptyList();

        Adapter(LayoutInflater layoutInflater) {
            this.layoutInflater = layoutInflater;
            setHasStableIds(true);
        }

        Observable<Object> getToggleRequests() {
            return toggleRequests;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            return new ViewHolder(layoutInflater.inflate(R.layout.v__selected_routes__item, parent, false));
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            RouteInfo info = routes.get(position);
            holder.itemView.setOnClickListener(o -> toggleRequests.onNext(Irrelevant.INSTANCE));
            ((SelectedRouteView) holder.itemView).setRouteId(info.route.id());
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
    @Named("showRoutes")
    Animator showRoutesAnimator;
    @Inject
    @Named("hideRoutes")
    Animator hideRoutesAnimator;
    @Inject
    CommonFunctions commonFunctions;
    @Inject
    LayoutInflater layoutInflater;
    @Inject
    Presenters presenters;

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
        // TODO: анимация? чтоб размер изменялся после удаления элементов
        itemsView.setLayoutManager(new LinearLayoutManager(getContext()));
        itemsView.setAdapter(new Adapter(layoutInflater));
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
                presenters.getRoutesPresenter().getResults()
                    .filter(Result::isSuccess)
                    .map(Result::getData),
                presenters.getPreferencesPresenter().getSelectedRoutes(),
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
    void attachToPresenters() {
        presenters.getRoutesPresenter().attach(this);
    }

    @Override
    void detachFromPresenters() {
        presenters.getRoutesPresenter().detach(this);
    }

    @Override
    public Observable<Object> getLoadRoutesRequests() {
        return Observable.just(Irrelevant.INSTANCE);
    }
}
