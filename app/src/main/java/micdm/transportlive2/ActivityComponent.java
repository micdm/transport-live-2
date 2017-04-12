package micdm.transportlive2;

import dagger.Subcomponent;
import micdm.transportlive2.ui.misc.MiscModule2;
import micdm.transportlive2.ui.misc.PermissionChecker;
import micdm.transportlive2.ui.views.AboutView;
import micdm.transportlive2.ui.views.CannotLoadView;
import micdm.transportlive2.ui.views.ClearableEditText;
import micdm.transportlive2.ui.views.CustomMapView;
import micdm.transportlive2.ui.views.LoadingView;
import micdm.transportlive2.ui.views.SearchRouteView;
import micdm.transportlive2.ui.views.SelectedRoutesView;
import micdm.transportlive2.ui.views.VehiclesView;

@ActivityScope
@Subcomponent(modules = {ActivityModule.class, AnimationModule.class, MiscModule2.class})
public interface ActivityComponent {

    void inject(MainActivity target);
    void inject(CannotLoadView target);
    void inject(CustomMapView target);
    void inject(LoadingView target);
    void inject(VehiclesView target);
    void inject(SearchRouteView target);
    void inject(SelectedRoutesView target);
    void inject(ClearableEditText target);
    void inject(AboutView target);
    void inject(PermissionChecker target);

    @Subcomponent.Builder
    interface Builder {
        Builder activityModule(ActivityModule module);
        ActivityComponent build();
    }
}
