package micdm.transportlive2;

import dagger.Subcomponent;
import micdm.transportlive2.ui.misc.MiscModule2;
import micdm.transportlive2.ui.misc.PermissionChecker;
import micdm.transportlive2.ui.views.AboutView;
import micdm.transportlive2.ui.views.CannotLoadView;
import micdm.transportlive2.ui.views.ClearableEditText;
import micdm.transportlive2.ui.views.CustomMapView;
import micdm.transportlive2.ui.views.ForecastView;
import micdm.transportlive2.ui.views.LoadingView;
import micdm.transportlive2.ui.views.MainToolbarView;
import micdm.transportlive2.ui.views.MapWrapperView;
import micdm.transportlive2.ui.views.ProbablyEmptyRecyclerView;
import micdm.transportlive2.ui.views.SearchResultsView;
import micdm.transportlive2.ui.views.SearchView;
import micdm.transportlive2.ui.views.SelectedRouteView;
import micdm.transportlive2.ui.views.SelectedRoutesView;
import micdm.transportlive2.ui.views.SelectedStationView;
import micdm.transportlive2.ui.views.SelectedStationsView;
import micdm.transportlive2.ui.views.VehiclesView;

@ActivityScope
@Subcomponent(modules = {ActivityModule.class, AnimationModule.class, MiscModule2.class})
public interface ActivityComponent {

    @Subcomponent.Builder
    interface Builder {
        Builder activityModule(ActivityModule module);
        ActivityComponent build();
    }

    void inject(MainActivity target);
    void inject(CannotLoadView target);
    void inject(CustomMapView target);
    void inject(LoadingView target);
    void inject(VehiclesView target);
    void inject(SearchView target);
    void inject(SelectedRoutesView target);
    void inject(ClearableEditText target);
    void inject(AboutView target);
    void inject(PermissionChecker target);
    void inject(ForecastView target);
    void inject(SelectedRouteView target);
    void inject(MainToolbarView target);
    void inject(SelectedStationsView target);
    void inject(SelectedStationView target);
    void inject(ProbablyEmptyRecyclerView target);
    void inject(SearchResultsView target);
    void inject(MapWrapperView target);
}
