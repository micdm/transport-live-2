package micdm.transportlive.ui;

import dagger.Module;
import dagger.Provides;
import micdm.transportlive.AppScope;

@Module
public class UiModule {

    @Provides
    @AppScope
    PresenterStore providePresenterStore() {
        return new PresenterStore();
    }
}
