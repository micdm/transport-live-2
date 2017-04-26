package micdm.transportlive2.ui.presenters;

import java.util.ArrayList;
import java.util.Collection;

import javax.inject.Inject;

import io.reactivex.Observable;
import io.reactivex.disposables.Disposable;
import io.reactivex.subjects.BehaviorSubject;
import io.reactivex.subjects.Subject;
import micdm.transportlive2.data.loaders.Loaders;
import micdm.transportlive2.data.loaders.Result;
import micdm.transportlive2.misc.Id;
import micdm.transportlive2.models.Path;
import micdm.transportlive2.ui.misc.ResultWatcherN;
import micdm.transportlive2.ui.misc.properties.ValueProperty;

public class PathsPresenter extends BasePresenter {

    public static class ViewInput {

        public final ValueProperty<Collection<Id>> routes = new ValueProperty<>();
    }

    @Inject
    Loaders loaders;

    public final ViewInput viewInput = new ViewInput();
    private final Subject<Result<Collection<Path>>> results = BehaviorSubject.create();

    @Override
    Disposable subscribeForEvents() {
        return subscribeForInput();
    }

    private Disposable subscribeForInput() {
        return viewInput.routes.get()
            .map(routeIds -> {
                Collection<Observable<Result<Path>>> observables = new ArrayList<>();
                for (Id routeId: routeIds) {
                    observables.add(loaders.getPathLoader(routeId).load());
                }
                return ResultWatcherN.newInstance(observables);
            })
            .switchMap(watcher ->
                Observable.<Result<Collection<Path>>>merge(
                    watcher.getLoading().map(o -> Result.newLoading()),
                    watcher.getSuccess().map(Result::newSuccess),
                    watcher.getFail().map(o -> Result.newFail())
                )
            )
            .subscribe(results::onNext);
    }

    public Observable<Result<Collection<Path>>> getResults() {
        return results;
    }
}
