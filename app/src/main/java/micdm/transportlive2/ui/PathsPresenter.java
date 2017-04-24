package micdm.transportlive2.ui;

import java.util.ArrayList;
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
import micdm.transportlive2.misc.CommonFunctions;
import micdm.transportlive2.misc.Id;
import micdm.transportlive2.models.Path;
import micdm.transportlive2.ui.misc.ResultWatcherN;

public class PathsPresenter extends BasePresenter {

    public static class ViewInput {

        private final Subject<Collection<Id>> loadPathsRequests = PublishSubject.create();

        Observable<Collection<Id>> getLoadPathsRequests() {
            return loadPathsRequests;
        }

        public void loadPaths(Collection<Id> ids) {
            loadPathsRequests.onNext(ids);
        }
    }

    @Inject
    CommonFunctions commonFunctions;
    @Inject
    Loaders loaders;

    public final ViewInput viewInput = new ViewInput();
    private final Subject<Result<Collection<Path>>> results = BehaviorSubject.create();

    @Override
    Disposable subscribeForEvents() {
        return new CompositeDisposable(
            subscribeForInput(),
            subscribeForResults()
        );
    }

    private Disposable subscribeForInput() {
        return viewInput.getLoadPathsRequests().subscribe(ids -> {
            for (Id id: ids) {
                loaders.getPathLoader(id).load();
            }
        });
    }

    private Disposable subscribeForResults() {
        return viewInput.getLoadPathsRequests()
            .map(routeIds -> {
                Collection<Observable<Result<Path>>> observables = new ArrayList<>();
                for (Id routeId: routeIds) {
                    observables.add(loaders.getPathLoader(routeId).getData());
                }
                return new ResultWatcherN<>(commonFunctions, observables);
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
