package micdm.transportlive.ui;

import java.util.Map;

import javax.inject.Inject;

import io.reactivex.Observable;
import micdm.transportlive.data.DataRepository;
import micdm.transportlive.data.Loader;
import micdm.transportlive.models.RouteGroup;

public class RoutesPresenter extends BasePresenter<RoutesPresenter.View> {

    interface View extends BasePresenter.View {

        Observable<Object> getLoadDataRequests();

        Observable<Object> getReloadDataRequests();
    }

    @Inject
    DataRepository dataRepository;
    @Inject
    Loader loader;

    Observable<Loader.LoadingState> getLoadingState() {
        return getViews()
            .flatMap(view ->
                Observable
                    .merge(
                        view.getLoadDataRequests().take(1),
                        view.getReloadDataRequests()
                    )
                    .switchMap(o -> loader.loadRoutes())
            );
    }

    Observable<Map<String, RouteGroup>> getRouteGroups() {
        return dataRepository.getRouteGroups();
    }
}
