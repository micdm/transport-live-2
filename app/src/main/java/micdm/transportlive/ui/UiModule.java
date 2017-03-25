package micdm.transportlive.ui;

import dagger.Module;
import dagger.Provides;
import micdm.transportlive.AppScope;
import micdm.transportlive.ui.misc.PaintConstructor;

@Module
public class UiModule {

    @Provides
    @AppScope
    PresenterStore providePresenterStore() {
        return new PresenterStore();
    }

    @Provides
    @AppScope
    PaintConstructor providePaintConstructor() {
        return new PaintConstructor();
    }
}
