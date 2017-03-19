package micdm.transportlive.ui.views;

import android.content.Context;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.TextView;

import org.javatuples.Pair;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.reactivex.Observable;
import io.reactivex.subjects.BehaviorSubject;
import io.reactivex.subjects.Subject;
import micdm.transportlive.ComponentHolder;
import micdm.transportlive.R;
import micdm.transportlive.models.Route;
import micdm.transportlive.models.RouteGroup;

public class RoutesView extends BaseView {

    static class Adapter extends RecyclerView.Adapter<Adapter.ViewHolder> {

        static class ViewHolder extends RecyclerView.ViewHolder {

            @BindView(R.id.v__routes_item__selected)
            CompoundButton selectedView;
            @BindView(R.id.v__routes_item__name)
            TextView nameView;

            ViewHolder(View itemView) {
                super(itemView);
                ButterKnife.bind(this, itemView);
            }
        }

        private final LayoutInflater layoutInflater;
        private final Subject<Set<String>> selectRoutesRequests = BehaviorSubject.create();
        private List<Pair<RouteGroup, Route>> routes;
        private Set<String> selectedRoutes = Collections.emptySet();

        private Adapter(LayoutInflater layoutInflater) {
            this.layoutInflater = layoutInflater;
        }

        Observable<Set<String>> getSelectRoutesRequests() {
            return selectRoutesRequests;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            return new ViewHolder(layoutInflater.inflate(R.layout.v__routes_item, parent, false));
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            Pair<RouteGroup, Route> pair = routes.get(position);
            holder.selectedView.setOnCheckedChangeListener(null);
            holder.selectedView.setChecked(selectedRoutes.contains(pair.getValue1().id()));
            holder.selectedView.setOnCheckedChangeListener((view, isChecked) -> {
                Set<String> copy = new HashSet<>(selectedRoutes);
                if (isChecked) {
                    copy.add(pair.getValue1().id());
                } else {
                    copy.remove(pair.getValue1().id());
                }
                selectRoutesRequests.onNext(copy);
            });
            holder.nameView.setText(String.format("%s %s", pair.getValue1().number(), pair.getValue0().name()));
        }

        @Override
        public int getItemCount() {
            return (routes == null) ? 0 : routes.size();
        }

        void setRouteGroups(Map<String, RouteGroup> groups) {
            routes = new ArrayList<>();
            for (RouteGroup group: groups.values()) {
                for (Route route: group.routes().values()) {
                    routes.add(Pair.with(group, route));
                }
            }
            Collections.sort(routes, (a, b) -> {
                if (a.getValue0().equals(b.getValue0())) {
                    return a.getValue1().number().compareTo(b.getValue1().number());
                }
                return a.getValue0().name().compareTo(b.getValue0().name());
            });
            notifyDataSetChanged();
        }

        void setSelectedRoutes(Set<String> routes) {
            selectedRoutes = routes;
            notifyDataSetChanged();
        }
    }

    @Inject
    Context context;
    @Inject
    LayoutInflater layoutInflater;

    @BindView(R.id.v__routes2__routes)
    RecyclerView routesView;

    public RoutesView(Context context, AttributeSet attrs) {
        super(context, attrs);
        if (!isInEditMode()) {
            ComponentHolder.getActivityComponent().inject(this);
        }
    }

    @Override
    protected void inflateContent(LayoutInflater layoutInflater) {
        layoutInflater.inflate(R.layout.v__routes, this);
    }

    @Override
    protected void setupViews() {
        routesView.setLayoutManager(new LinearLayoutManager(context));
        routesView.setAdapter(new Adapter(layoutInflater));
    }

    public void setRouteGroups(Map<String, RouteGroup> groups) {
        ((Adapter) routesView.getAdapter()).setRouteGroups(groups);
    }

    public Observable<Set<String>> getSelectedRoutes() {
        return ((Adapter) routesView.getAdapter()).getSelectRoutesRequests();
    }

    public void setSelectedRoutes(Set<String> routes) {
        ((Adapter) routesView.getAdapter()).setSelectedRoutes(routes);
    }
}
