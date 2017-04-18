package micdm.transportlive2.ui;

import java.util.Collection;

import javax.inject.Inject;

import io.reactivex.Observable;
import io.reactivex.disposables.Disposable;
import io.reactivex.subjects.PublishSubject;
import io.reactivex.subjects.Subject;
import micdm.transportlive2.data.loaders.Loaders;
import micdm.transportlive2.data.loaders.Result;
import micdm.transportlive2.data.loaders.RoutesLoader;
import micdm.transportlive2.models.RouteGroup;

public class RoutesPresenter extends BasePresenter<RoutesPresenter.View, RoutesPresenter.ViewInput> implements RoutesLoader.Client {

    public interface View {

        Observable<Object> getLoadRoutesRequests();
    }

    static class ViewInput extends BasePresenter.ViewInput<View> {

        private final Subject<Object> loadRoutesRequests = PublishSubject.create();

        Observable<Object> getLoadRoutesRequests() {
            return loadRoutesRequests;
        }

        @Override
        Disposable subscribeForInput(View view) {
            return view.getLoadRoutesRequests().subscribe(loadRoutesRequests::onNext);
        }
    }

    @Inject
    Loaders loaders;

    @Override
    ViewInput newViewInput() {
        return new ViewInput();
    }

    @Override
    void attachToLoaders() {
        loaders.getRoutesLoader().attach(this);
    }

    @Override
    public Observable<Object> getLoadRoutesRequests() {
        return viewInput.getLoadRoutesRequests();
    }

    public Observable<Result<Collection<RouteGroup>>> getResults() {
        return loaders.getRoutesLoader().getData();
    }
}
