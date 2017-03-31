package micdm.transportlive.ui;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import micdm.transportlive.ComponentHolder;

class PresenterStore {

    private interface PresenterFactory<T extends BasePresenter> {
        T newInstance();
    }

    private final Map<List<Object>, BasePresenter<?>> presenters = new HashMap<>();

    RoutesPresenter getRoutesPresenter(RoutesPresenter.View view) {
        RoutesPresenter presenter = getOrCreate(Collections.singletonList(RoutesPresenter.class), RoutesPresenter::new);
        ComponentHolder.getAppComponent().inject(presenter);
        initPresenter(presenter);
        handleView(presenter, view);
        return presenter;
    }

    SelectedRoutesPresenter getSelectedRoutesPresenter(SelectedRoutesPresenter.View view) {
        SelectedRoutesPresenter presenter = getOrCreate(Collections.singletonList(SelectedRoutesPresenter.class), SelectedRoutesPresenter::new);
        ComponentHolder.getAppComponent().inject(presenter);
        initPresenter(presenter);
        handleView(presenter, view);
        return presenter;
    }

    VehiclesPresenter getVehiclesPresenter(VehiclesPresenter.View view) {
        VehiclesPresenter presenter = getOrCreate(Collections.singletonList(VehiclesPresenter.class), VehiclesPresenter::new);
        ComponentHolder.getAppComponent().inject(presenter);
        initPresenter(presenter);
        handleView(presenter, view);
        return presenter;
    }

    PathsPresenter getPathsPresenter(PathsPresenter.View view) {
        PathsPresenter presenter = getOrCreate(Collections.singletonList(PathsPresenter.class), PathsPresenter::new);
        ComponentHolder.getAppComponent().inject(presenter);
        initPresenter(presenter);
        handleView(presenter, view);
        return presenter;
    }

    private <T extends BasePresenter> T getOrCreate(List<Object> key, PresenterFactory<T> factory) {
        T presenter = (T) presenters.get(key);
        if (presenter == null) {
            presenter = factory.newInstance();
            presenters.put(key, presenter);
        }
        return presenter;
    }

    private void initPresenter(BasePresenter presenter) {
        if (!presenter.isInitialized()) {
            presenter.init();
        }
    }

    private <T1 extends BasePresenter.View, T2 extends BasePresenter<T1>> void handleView(T2 presenter, T1 view) {
        if (!presenter.hasView(view)) {
            if (view.isAttached()) {
                presenter.attachView(view);
            } else {
                view.getAttaches()
                    .take(1)
                    .subscribe(o -> presenter.attachView(view));
            }
            view.getDetaches()
                .take(1)
                .subscribe(o -> presenter.detachView(view));
        }
    }
}
