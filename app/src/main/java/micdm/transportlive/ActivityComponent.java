package micdm.transportlive;

import dagger.Subcomponent;
import micdm.transportlive.ui.views.VehiclesView;
import micdm.transportlive.ui.misc.MarkerIconBuilder;
import micdm.transportlive.ui.views.AboutView;
import micdm.transportlive.ui.views.CannotLoadView;
import micdm.transportlive.ui.views.ClearableEditText;
import micdm.transportlive.ui.views.CustomMapView;
import micdm.transportlive.ui.views.LoadingView;
import micdm.transportlive.ui.views.SearchRouteView;
import micdm.transportlive.ui.views.SelectedRoutesView;

@ActivityScope
@Subcomponent(modules = {ActivityModule.class, AnimationModule.class})
public interface ActivityComponent {

    void inject(MainActivity target);
    void inject(CannotLoadView target);
    void inject(CustomMapView target);
    void inject(LoadingView target);
    void inject(MarkerIconBuilder target);
    void inject(VehiclesView target);
    void inject(SearchRouteView target);
    void inject(SelectedRoutesView target);
    void inject(ClearableEditText target);
    void inject(AboutView target);

    @Subcomponent.Builder
    interface Builder {
        Builder activityModule(ActivityModule module);
        ActivityComponent build();
    }
}
