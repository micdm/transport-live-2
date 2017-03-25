package micdm.transportlive.ui;

import javax.inject.Inject;

import io.reactivex.Observable;
import micdm.transportlive.data.RoutesLoader;

public class RoutesPresenter extends BasePresenter<RoutesPresenter.View> implements RoutesLoader.Client {

    interface View extends BasePresenter.View {

        Observable<Object> getLoadRoutesRequests();
        Observable<Object> getReloadRoutesRequests();
    }

    @Inject
    RoutesLoader routesLoader;

    @Override
    void initMore() {
        routesLoader.attach(this);
    }

    @Override
    public Observable<Object> getLoadRoutesRequests() {
        return getViewInput(View::getLoadRoutesRequests);
    }

    @Override
    public Observable<Object> getReloadRoutesRequests() {
        return getViewInput(View::getReloadRoutesRequests);
    }

    Observable<RoutesLoader.State> getLoaderStates() {
        return routesLoader.getData();
    }
}
