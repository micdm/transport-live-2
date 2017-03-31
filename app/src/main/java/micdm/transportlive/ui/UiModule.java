package micdm.transportlive.ui;

import dagger.Module;
import dagger.Provides;
import micdm.transportlive.AppScope;
import micdm.transportlive.ComponentHolder;
import micdm.transportlive.ui.misc.ColorConstructor;
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
        PaintConstructor instance = new PaintConstructor();
        ComponentHolder.getAppComponent().inject(instance);
        return instance;
    }

    @Provides
    @AppScope
    ColorConstructor provideColorConstructor() {
        return new ColorConstructor();
    }
}
