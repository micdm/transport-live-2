package micdm.transportlive2.ui.presenters;

import java.util.Collection;

import javax.inject.Inject;

import io.reactivex.Observable;
import io.reactivex.disposables.Disposable;
import io.reactivex.subjects.BehaviorSubject;
import io.reactivex.subjects.Subject;
import micdm.transportlive2.data.loaders.Loaders;
import micdm.transportlive2.data.loaders.Result;
import micdm.transportlive2.models.RouteGroup;
import micdm.transportlive2.ui.misc.properties.NoValueProperty;

public class RoutesPresenter extends BasePresenter {

    public static class ViewInput {

        public final NoValueProperty loadRoutes = new NoValueProperty();
    }

    @Inject
    Loaders loaders;

    public final ViewInput viewInput = new ViewInput();
    private final Subject<Result<Collection<RouteGroup>>> results = BehaviorSubject.create();

    @Override
    Disposable subscribeForEvents() {
        return subscribeForInput();
    }

    private Disposable subscribeForInput() {
        return viewInput.loadRoutes.get()
            .switchMap(o -> loaders.getRoutesLoader().load())
            .subscribe(results::onNext);
    }

    public Observable<Result<Collection<RouteGroup>>> getResults() {
        return results;
    }
}
