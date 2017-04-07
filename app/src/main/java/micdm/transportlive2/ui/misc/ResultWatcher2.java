package micdm.transportlive2.ui.misc;

import io.reactivex.Observable;
import micdm.transportlive2.data.loaders.Result;
import micdm.transportlive2.misc.CommonFunctions;

public class ResultWatcher2<T1, T2> {

    public static class Product<T1, T2> {

        public final T1 first;
        public final T2 second;

        public Product(T1 first, T2 second) {
            this.first = first;
            this.second = second;
        }
    }

    private final CommonFunctions commonFunctions;
    private final Observable<Result<T1>> first;
    private final Observable<Result<T2>> second;

    public ResultWatcher2(CommonFunctions commonFunctions, Observable<Result<T1>> first, Observable<Result<T2>> second) {
        this.commonFunctions = commonFunctions;
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
        return Observable.combineLatest(first, second, Product::new);
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
