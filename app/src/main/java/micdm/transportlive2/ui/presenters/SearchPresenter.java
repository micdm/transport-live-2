package micdm.transportlive2.ui.presenters;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

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
import micdm.transportlive2.misc.Irrelevant;
import micdm.transportlive2.models.ImmutableRouteInfo;
import micdm.transportlive2.models.Route;
import micdm.transportlive2.models.RouteGroup;
import micdm.transportlive2.models.RouteInfo;
import micdm.transportlive2.models.Station;
import micdm.transportlive2.ui.misc.MiscFunctions;
import micdm.transportlive2.ui.misc.ResultWatcher2;

public class SearchPresenter extends BasePresenter {

    public static class SearchResult {

        public final Collection<RouteInfo> routes;
        public final Collection<Station> stations;

        SearchResult(Collection<RouteInfo> routes, Collection<Station> stations) {
            this.routes = routes;
            this.stations = stations;
        }
    }

    public static class ViewInput {

        private final Subject<CharSequence> searchRequests = PublishSubject.create();
        private final Subject<Object> resetRequests = PublishSubject.create();

        Observable<CharSequence> getSearchRequests() {
            return searchRequests;
        }

        Observable<Object> getResetRequests() {
            return resetRequests;
        }

        public void search(CharSequence query) {
            searchRequests.onNext(query);
        }

        public void reset() {
            resetRequests.onNext(Irrelevant.INSTANCE);
        }
    }

    @Inject
    CommonFunctions commonFunctions;
    @Inject
    Loaders loaders;
    @Inject
    MiscFunctions miscFunctions;

    public final ViewInput viewInput = new ViewInput();
    private final Subject<CharSequence> searchQuery = BehaviorSubject.create();
    private final Subject<Result<SearchResult>> searchResults = BehaviorSubject.create();
    private final Subject<Object> resetRequests = PublishSubject.create();

    @Override
    Disposable subscribeForEvents() {
        return subscribeForInput();
    }

    private Disposable subscribeForInput() {
        return new CompositeDisposable(
            subscribeForSetSearchQueryRequests(),
            subscribeForSearchRequests(),
            subscribeForResetRequests()
        );
    }

    private Disposable subscribeForSetSearchQueryRequests() {
        return viewInput.getSearchRequests().subscribe(searchQuery::onNext);
    }

    private Disposable subscribeForSearchRequests() {
        Observable<String> common = viewInput.getSearchRequests().map(query -> query.toString().toLowerCase());
        ResultWatcher2<Collection<RouteInfo>, Collection<Station>> watcher = ResultWatcher2.newInstance(
            common.switchMap(query -> {
                if (query.isEmpty()) {
                    return Observable.just(Result.newSuccess(Collections.emptyList()));
                }
                return getRoutes(query);
            }),
            common.switchMap(query -> {
                if (query.isEmpty()) {
                    return Observable.just(Result.newSuccess(Collections.emptyList()));
                }
                return loaders.getSearchStationsLoader(query).load();
            })
        );
        return Observable
            .<Result<SearchResult>>merge(
                watcher.getLoading().map(o -> Result.newLoading()),
                watcher.getSuccess().map(product -> Result.newSuccess(new SearchResult(product.first, product.second))),
                watcher.getFail().map(o -> Result.newFail())
            )
            .subscribe(searchResults::onNext);
    }

    private Observable<Result<Collection<RouteInfo>>> getRoutes(String query) {
        Observable<Result<Collection<RouteGroup>>> common = loaders.getRoutesLoader().load().share();
        return Observable.merge(
            common
                .filter(Result::isLoading)
                .map(o -> Result.newLoading()),
            common
                .filter(Result::isSuccess)
                .map(Result::getData)
                .map(groups -> {
                    Collection<RouteInfo> routes = new ArrayList<>();
                    for (RouteGroup group: groups) {
                        for (Route route: group.routes()) {
                            if ((isRouteGroupMatchesSearch(group, query) || isRouteMatchesSearch(route, query))) {
                                routes.add(
                                    ImmutableRouteInfo.builder()
                                        .group(group)
                                        .route(route)
                                        .build()
                                );
                            }
                        }
                    }
                    return Result.newSuccess(routes);
                }),
            common
                .filter(Result::isFail)
                .map(o -> Result.newFail())
        );
    }

    private boolean isRouteGroupMatchesSearch(RouteGroup group, CharSequence search) {
        return miscFunctions.getRouteGroupName(group).toString().toLowerCase().contains(search);
    }

    private boolean isRouteMatchesSearch(Route route, CharSequence search) {
        return route.number().contains(search) ||
            route.source().toLowerCase().contains(search) ||
            route.destination().toLowerCase().contains(search);
    }

    private Disposable subscribeForResetRequests() {
        return viewInput.getResetRequests().subscribe(resetRequests::onNext);
    }

    public Observable<CharSequence> getSearchQuery() {
        return searchQuery;
    }

    public Observable<Result<SearchResult>> getSearchResults() {
        return searchResults;
    }

    public Observable<Object> getResetRequests() {
        return resetRequests;
    }
}
