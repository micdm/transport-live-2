package micdm.transportlive.ui.misc;

import dagger.Module;
import dagger.Provides;
import micdm.transportlive.AppScope;
import micdm.transportlive.ComponentHolder;

@Module
public class MiscModule {

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

    @Provides
    @AppScope
    MiscFunctions provideMiscFunctions() {
        MiscFunctions instance = new MiscFunctions();
        ComponentHolder.getAppComponent().inject(instance);
        return instance;
    }
}
