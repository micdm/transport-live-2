package micdm.transportlive2.ui;

import java.util.HashMap;
import java.util.Map;

import micdm.transportlive2.ComponentHolder;
import micdm.transportlive2.misc.Container;
import micdm.transportlive2.misc.Id;

public class Presenters extends Container<BasePresenter> {

    private final Map<Id, ForecastPresenter> forecastPresenters = new HashMap<>();
    private final Map<Id, VehiclesPresenter> vehiclesPresenters = new HashMap<>();

    public ForecastPresenter getForecastPresenter(Id stationId) {
        return getOrCreateInstance(forecastPresenters, stationId, () -> {
            ForecastPresenter instance = new ForecastPresenter(stationId);
            ComponentHolder.getAppComponent().inject(instance);
            return instance;
        });
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
