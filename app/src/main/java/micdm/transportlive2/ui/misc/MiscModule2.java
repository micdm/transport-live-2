package micdm.transportlive2.ui.misc;

import dagger.Module;
import dagger.Provides;
import micdm.transportlive2.ActivityScope;
import micdm.transportlive2.ComponentHolder;

@Module
public class MiscModule2 {

    @Provides
    @ActivityScope
    PermissionChecker providePermissionChecker() {
        PermissionChecker instance = new PermissionChecker();
        ComponentHolder.getActivityComponent().inject(instance);
        return instance;
    }
}
