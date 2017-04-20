package micdm.transportlive2.ui;

import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;

import micdm.transportlive2.ComponentHolder;
import micdm.transportlive2.misc.Container;
import micdm.transportlive2.misc.Id;

public class Presenters extends Container<BasePresenter> {

    @Inject
    AllVehiclesPresenter allVehiclesPresenter;
    @Inject
    PathsPresenter pathsPresenter;
    @Inject
    PreferencesPresenter preferencesPresenter;
    @Inject
    RoutesPresenter routesPresenter;

    private final Map<Id, ForecastPresenter> forecastPresenters = new HashMap<>();
    private final Map<Id, VehiclesPresenter> vehiclesPresenters = new HashMap<>();

    public AllVehiclesPresenter getAllVehiclesPresenter() {
        return allVehiclesPresenter;
    }

    public ForecastPresenter getForecastPresenter(Id stationId) {
        return getOrCreateInstance(forecastPresenters, stationId, () -> {
            ForecastPresenter instance = new ForecastPresenter(stationId);
            ComponentHolder.getAppComponent().inject(instance);
            return instance;
        });
    }

    public PathsPresenter getPathsPresenter() {
        return pathsPresenter;
    }

    public PreferencesPresenter getPreferencesPresenter() {
        return preferencesPresenter;
    }

    public RoutesPresenter getRoutesPresenter() {
        return routesPresenter;
    }

    public VehiclesPresenter getVehiclesPresenter(Id routeId) {
        return getOrCreateInstance(vehiclesPresenters, routeId, () -> {
            VehiclesPresenter instance = new VehiclesPresenter(routeId);
            ComponentHolder.getAppComponent().inject(instance);
            return instance;
        });
    }

    @Override
    protected void onNewInstance(BasePresenter instance) {
        instance.init();
    }
}
