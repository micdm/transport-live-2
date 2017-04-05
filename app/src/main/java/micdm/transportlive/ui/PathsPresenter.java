package micdm.transportlive.ui;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

import javax.inject.Inject;

import io.reactivex.Observable;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import micdm.transportlive.data.loaders.Loaders;
import micdm.transportlive.data.loaders.PathLoader;
import micdm.transportlive.data.loaders.Result;
import micdm.transportlive.misc.CommonFunctions;
import micdm.transportlive.misc.Id;
import micdm.transportlive.models.Path;
import micdm.transportlive.ui.misc.ResultWatcherN;

public class PathsPresenter extends BasePresenter<PathsPresenter.View> implements PathLoader.Client {

    interface View extends BasePresenter.View {

        Observable<Collection<Id>> getLoadPathsRequests();
    }

    @Inject
    CommonFunctions commonFunctions;
    @Inject
    Loaders loaders;

    @Override
    Disposable subscribeForEvents() {
        return new CompositeDisposable(
            getPathLoadersToAttach().subscribe(loader -> loader.attach(this)),
            getPathLoadersToDetach().subscribe(loader -> loader.detach(this))
        );
    }

    private Observable<PathLoader> getPathLoadersToAttach() {
        Observable<Collection<Id>> common = getViewInput(View::getLoadPathsRequests);
        return commonFunctions
            .getDelta(
                common
                    .compose(commonFunctions.getPrevious())
                    .startWith(Collections.<Id>emptyList()),
                common
            )
            .switchMap(Observable::fromIterable)
            .map(loaders::getPathLoader);
    }

    private Observable<PathLoader> getPathLoadersToDetach() {
        Observable<Collection<Id>> common = getViewInput(View::getLoadPathsRequests);
        return commonFunctions
            .getDelta(
                common.skip(1),
                common.compose(commonFunctions.getPrevious())
            )
            .switchMap(Observable::fromIterable)
            .map(loaders::getPathLoader);
    }

    @Override
    public Observable<Id> getLoadPathRequests() {
        return getViewInput(View::getLoadPathsRequests).switchMap(Observable::fromIterable);
    }

    Observable<Result<Collection<Path>>> getResults() {
        return Observable
            .<Consumer<Collection<PathLoader>>>merge(
                getPathLoadersToAttach().map(loader -> accumulated -> accumulated.add(loader)),
                getPathLoadersToDetach().map(loader -> accumulated -> accumulated.remove(loader))
            )
            .scan(new ArrayList<PathLoader>(), (accumulated, handler) -> {
                handler.accept(accumulated);
                return accumulated;
            })
            .switchMap(loaders -> {
                Collection<Observable<Result<Path>>> observables = new ArrayList<>(loaders.size());
                for (PathLoader loader: loaders) {
                    observables.add(loader.getData());
                }
                ResultWatcherN<Path> watcher = new ResultWatcherN<>(commonFunctions, observables);
                return Observable.merge(
                    watcher.getLoading().map(o -> Result.newLoading()),
                    watcher.getSuccess().map(Result::newSuccess),
                    watcher.getFail().map(o -> Result.newFail())
                );
            });
    }
}
