package micdm.transportlive2.ui.misc;

import io.reactivex.Observable;
import micdm.transportlive2.ComponentHolder;
import micdm.transportlive2.data.loaders.Result;
import micdm.transportlive2.misc.CommonFunctions;
import micdm.transportlive2.misc.ObservableCache;

public class ResultWatcher2<T1, T2> {

    public static class Product<T1, T2> {

        public final T1 first;
        public final T2 second;

        public Product(T1 first, T2 second) {
            this.first = first;
            this.second = second;
        }
    }

    public static <T1, T2> ResultWatcher2<T1, T2> newInstance(Observable<Result<T1>> first, Observable<Result<T2>> second) {
        return new ResultWatcher2<>(
            ComponentHolder.getAppComponent().getCommonFunctions(),
            ComponentHolder.getAppComponent().getObservableCache(),
            first, second);
    }

    private final CommonFunctions commonFunctions;
    private final ObservableCache observableCache;
    private final Observable<Result<T1>> first;
    private final Observable<Result<T2>> second;

    private ResultWatcher2(CommonFunctions commonFunctions, ObservableCache observableCache,
                           Observable<Result<T1>> first, Observable<Result<T2>> second) {
        this.commonFunctions = commonFunctions;
        this.observableCache = observableCache;
        this.first = first;
        this.second = second;
    }

    public Observable<Object> getLoading() {
        return getLatest()
            .filter(product -> !isFail(product) && isLoading(product))
            .compose(commonFunctions.toNothing());
    }

    public Observable<Product<T1, T2>> getSuccess() {
        return getLatest()
            .filter(this::isSuccess)
            .map(product -> new Product<>(product.first.getData(), product.second.getData()));
    }

    public Observable<Object> getFail() {
        return getLatest()
            .filter(this::isFail)
            .compose(commonFunctions.toNothing());
    }

    private Observable<Product<Result<T1>, Result<T2>>> getLatest() {
        return observableCache.get("getLatest", () -> Observable.combineLatest(first, second, Product::new).share());
    }

    private boolean isLoading(Product<Result<T1>, Result<T2>> product) {
        return product.first.isLoading() || product.second.isLoading();
    }

    private boolean isSuccess(Product<Result<T1>, Result<T2>> product) {
        return product.first.isSuccess() && product.second.isSuccess();
    }

    private boolean isFail(Product<Result<T1>, Result<T2>> product) {
        return product.first.isFail() || product.second.isFail();
    }
}
