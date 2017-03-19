package micdm.transportlive;

import javax.inject.Named;

import dagger.Module;
import dagger.Provides;
import io.reactivex.Scheduler;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

@Module
public class RxModule {

    @Provides
    @AppScope
    @Named("io")
    Scheduler provideIoScheduler() {
        return Schedulers.io();
    }

    @Provides
    @AppScope
    @Named("mainThread")
    Scheduler provideMainThreadScheduler() {
        return AndroidSchedulers.mainThread();
    }
}
