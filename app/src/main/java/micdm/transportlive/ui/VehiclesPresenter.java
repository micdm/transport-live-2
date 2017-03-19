package micdm.transportlive.ui;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import io.reactivex.Observable;
import micdm.transportlive.data.DataRepository;
import micdm.transportlive.data.Loader;
import micdm.transportlive.data.Loader.LoadingState;
import micdm.transportlive.models.RouteGroup;

public class VehiclesPresenter extends BasePresenter<VehiclesPresenter.View> {

    interface View extends BasePresenter.View {

        Observable<Object> getLoadDataRequests();
        Observable<Object> getReloadDataRequests();
    }

    @Inject
    DataRepository dataRepository;
    @Inject
    Loader loader;

    Observable<LoadingState> getLoadingState() {
        return getViews()
            .flatMap(view ->
                Observable
                    .merge(
                        view.getLoadDataRequests().take(1),
                        view.getReloadDataRequests()
                    )
                    .withLatestFrom(dataRepository.getSelectedRoutes(), (o, routes) -> {
                        List<Observable<LoadingState>> observables = new ArrayList<>();
                        for (String routeId: routes) {
                            observables.add(loader.loadVehicles(routeId));
                        }
                        return observables;
                    })
                    .switchMap(observables ->
                        Observable.combineLatest(observables, results -> {
                            List<Object> list = Arrays.asList(results);
                            if (list.contains(LoadingState.START)) {
                                return LoadingState.START;
                            }
                            if (list.contains(LoadingState.FAIL)) {
                                return LoadingState.FAIL;
                            }
                            return LoadingState.SUCCESS;
                        })
                    )
            );
    }

    Observable<Map<String, RouteGroup>> getRouteGroups() {
        return dataRepository.getRouteGroups();
    }
}
