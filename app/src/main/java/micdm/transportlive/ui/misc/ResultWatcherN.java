package micdm.transportlive.ui.misc;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.Observable;
import micdm.transportlive.data.loaders.Result;
import micdm.transportlive.misc.CommonFunctions;

public class ResultWatcherN<T> {

    private final CommonFunctions commonFunctions;
    private final Iterable<Observable<Result<T>>> observables;

    public ResultWatcherN(CommonFunctions commonFunctions, Iterable<Observable<Result<T>>> observables) {
        this.commonFunctions = commonFunctions;
        this.observables = observables;
    }

    public Observable<Object> getLoading() {
        return getLatest()
            .filter(results -> !isFail(results))
            .filter(this::isLoading)
            .compose(commonFunctions.toNothing());
    }

    public Observable<List<T>> getSuccess() {
        return getLatest()
            .filter(this::isSuccess)
            .map(results -> {
                List<T> datas = new ArrayList<>(results.size());
                for (Result<T> result: results) {
                    datas.add(result.getData());
                }
                return datas;
            });
    }

    public Observable<Object> getFail() {
        return getLatest()
            .filter(this::isFail)
            .compose(commonFunctions.toNothing());
    }

    private Observable<List<Result<T>>> getLatest() {
        return Observable.combineLatest(
            observables,
            (Object... objects) -> {
                List<Result<T>> results = new ArrayList<>(objects.length);
                for (Object object: objects) {
                    results.add((Result<T>) object);
                }
                return results;
            }
        );
    }

    private boolean isLoading(List<Result<T>> results) {
        for (Result result: results) {
            if (result.isLoading()) {
                return true;
            }
        }
        return false;
    }

    private boolean isSuccess(List<Result<T>> results) {
        for (Result result: results) {
            if (!result.isSuccess()) {
                return false;
            }
        }
        return true;
    }

    private boolean isFail(List<Result<T>> results) {
        for (Result result: results) {
            if (result.isFail()) {
                return true;
            }
        }
        return false;
    }
}
