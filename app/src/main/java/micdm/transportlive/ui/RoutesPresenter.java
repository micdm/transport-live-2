package micdm.transportlive.ui;

import java.util.Collection;

import javax.inject.Inject;

import io.reactivex.Observable;
import micdm.transportlive.data.loaders.Loaders;
import micdm.transportlive.data.loaders.Result;
import micdm.transportlive.data.loaders.RoutesLoader;
import micdm.transportlive.models.RouteGroup;

public class RoutesPresenter extends BasePresenter<RoutesPresenter.View> implements RoutesLoader.Client {

    public interface View {

        Observable<Object> getLoadRoutesRequests();
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

    public Observable<Result<Collection<RouteGroup>>> getResults() {
        return loaders.getRoutesLoader().getData();
    }
}
