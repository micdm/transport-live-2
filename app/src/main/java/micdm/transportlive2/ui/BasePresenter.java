package micdm.transportlive2.ui;

import io.reactivex.disposables.Disposable;

abstract class BasePresenter {

    void init() {
        subscribeForEvents();
    }

    Disposable subscribeForEvents() {
        return null;
    }
}
