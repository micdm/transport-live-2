package micdm.transportlive.ui;

import java.util.Collection;

import javax.inject.Inject;

import io.reactivex.Observable;
import micdm.transportlive.data.loaders.Loaders;
import micdm.transportlive.data.loaders.Result;
import micdm.transportlive.data.loaders.RoutesLoader;
import micdm.transportlive.models.RouteGroup;

public class RoutesPresenter extends BasePresenter<RoutesPresenter.View> implements RoutesLoader.Client {

    interface View extends BasePresenter.View {

        Observable<Object> getLoadRoutesRequests();
        Observable<Object> getReloadRoutesRequests();
    }

    @Inject
    Loaders loaders;

    @Override
    void initMore() {
        loaders.getRoutesLoader().attach(this);
    }

    @Override
    public Observable<Object> getLoadRoutesRequests() {
        return getViewInput(View::getLoadRoutesRequests);
    }

    @Override
    public Observable<Object> getReloadRoutesRequests() {
        return getViewInput(View::getReloadRoutesRequests);
    }

    @Override
    public Observable<Object> getCancelRoutesLoadingRequests() {
        return Observable.empty();
    }

    Observable<Result<Collection<RouteGroup>>> getResults() {
        return loaders.getRoutesLoader().getData();
    }
}
