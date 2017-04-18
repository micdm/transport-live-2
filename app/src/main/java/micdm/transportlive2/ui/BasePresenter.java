package micdm.transportlive2.ui;

import java.util.HashMap;
import java.util.Map;

import io.reactivex.disposables.Disposable;

abstract class BasePresenter<V, VI extends BasePresenter.ViewInput<V>> {

    static abstract class ViewInput<View> {

        private final Map<View, Disposable> subscriptions = new HashMap<>();

        void attach(View view) {
            subscriptions.put(view, subscribeForInput(view));
        }

        abstract Disposable subscribeForInput(View view);

        void detach(View view) {
            subscriptions.remove(view).dispose();
        }
    }

    final VI viewInput = newViewInput();

    abstract VI newViewInput();

    void init() {
        subscribeForEvents();
        attachToLoaders();
    }

    void attachToLoaders() {}

    Disposable subscribeForEvents() {
        return null;
    }

    public void attach(V view) {
        viewInput.attach(view);
    }

    public void detach(V view) {
        viewInput.detach(view);
    }
}
