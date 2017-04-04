package micdm.transportlive.ui;

import dagger.Module;
import dagger.Provides;
import micdm.transportlive.AppScope;
import micdm.transportlive.ui.misc.MiscModule;

@Module(includes = {MiscModule.class})
public class UiModule {

    @Provides
    @AppScope
    PresenterStore providePresenterStore() {
        return new PresenterStore();
    }
}
