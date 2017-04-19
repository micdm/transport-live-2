package micdm.transportlive2.ui;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

import javax.inject.Inject;

import io.reactivex.Observable;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.subjects.PublishSubject;
import io.reactivex.subjects.Subject;
import micdm.transportlive2.data.loaders.Loaders;
import micdm.transportlive2.data.loaders.PathLoader;
import micdm.transportlive2.data.loaders.Result;
import micdm.transportlive2.misc.CommonFunctions;
import micdm.transportlive2.misc.Id;
import micdm.transportlive2.models.Path;
import micdm.transportlive2.ui.misc.ResultWatcherN;

public class PathsPresenter extends BasePresenter<PathsPresenter.View, PathsPresenter.ViewInput> implements PathLoader.Client {

    public interface View {

        Observable<Collection<Id>> getLoadPathsRequests();
    }

    static class ViewInput extends BasePresenter.ViewInput<View> {

        private final Subject<Collection<Id>> loadPathsRequests = PublishSubject.create();

        Observable<Collection<Id>> getLoadPathsRequests() {
            return loadPathsRequests;
        }

        @Override
        Disposable subscribeForInput(View view) {
            return view.getLoadPathsRequests().subscribe(loadPathsRequests::onNext);
        }
    }

    @Inject
    CommonFunctions commonFunctions;
    @Inject
    Loaders loaders;

    PathsPresenter() {
        super(new ViewInput());
    }

    @Override
    Disposable subscribeForEvents() {
        return new CompositeDisposable(
            getPathLoadersToAttach().subscribe(loader -> loader.attach(this)),
            getPathLoadersToDetach().subscribe(loader -> loader.detach(this))
        );
    }

    private Observable<PathLoader> getPathLoadersToAttach() {
        return commonFunctions
            .getDelta(
                viewInput.getLoadPathsRequests()
                    .compose(commonFunctions.getPrevious())
                    .startWith(Collections.<Id>emptyList()),
                viewInput.getLoadPathsRequests()
            )
            .switchMap(Observable::fromIterable)
            .map(loaders::getPathLoader);
    }

    private Observable<PathLoader> getPathLoadersToDetach() {
        return commonFunctions
            .getDelta(
                viewInput.getLoadPathsRequests().skip(1),
                viewInput.getLoadPathsRequests().compose(commonFunctions.getPrevious())
            )
            .switchMap(Observable::fromIterable)
            .map(loaders::getPathLoader);
    }

    @Override
    public Observable<Id> getLoadPathRequests() {
        return viewInput.getLoadPathsRequests().switchMap(Observable::fromIterable);
    }

    public Observable<Result<Collection<Path>>> getResults() {
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
