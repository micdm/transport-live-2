package micdm.transportlive2.ui;

import java.util.Collection;

import javax.inject.Inject;

import io.reactivex.Observable;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.subjects.BehaviorSubject;
import io.reactivex.subjects.PublishSubject;
import io.reactivex.subjects.Subject;
import micdm.transportlive2.data.loaders.Loaders;
import micdm.transportlive2.data.loaders.Result;
import micdm.transportlive2.misc.Irrelevant;
import micdm.transportlive2.models.RouteGroup;

public class RoutesPresenter extends BasePresenter {

    public static class ViewInput {

        private final Subject<Object> loadRoutesRequests = PublishSubject.create();

        Observable<Object> getLoadRoutesRequests() {
            return loadRoutesRequests;
        }

        public void loadRoutes() {
            loadRoutesRequests.onNext(Irrelevant.INSTANCE);
        }
    }

    @Inject
    Loaders loaders;

    public final ViewInput viewInput = new ViewInput();
    private final Subject<Result<Collection<RouteGroup>>> results = BehaviorSubject.create();

    @Override
    Disposable subscribeForEvents() {
        return new CompositeDisposable(
            subscribeForInput(),
            subscribeForResults()
        );
    }

    private Disposable subscribeForInput() {
        return viewInput.getLoadRoutesRequests().subscribe(o -> loaders.getRoutesLoader().load());
    }

    private Disposable subscribeForResults() {
        return loaders.getRoutesLoader().getData().subscribe(results::onNext);
    }

    public Observable<Result<Collection<RouteGroup>>> getResults() {
        return results;
    }
}
