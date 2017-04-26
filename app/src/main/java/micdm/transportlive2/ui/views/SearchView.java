package micdm.transportlive2.ui.views;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;

import org.joda.time.Duration;

import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import butterknife.BindView;
import io.reactivex.Observable;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import micdm.transportlive2.ComponentHolder;
import micdm.transportlive2.R;
import micdm.transportlive2.data.loaders.Result;
import micdm.transportlive2.misc.CommonFunctions;
import micdm.transportlive2.ui.presenters.Presenters;
import micdm.transportlive2.ui.presenters.SearchPresenter;

public class SearchView extends PresentedView {

    private static final Duration SEARCH_DELAY = Duration.millis(500);

    @Inject
    CommonFunctions commonFunctions;
    @Inject
    Presenters presenters;

    @BindView(R.id.v__search__input)
    ClearableEditText inputView;

    public SearchView(Context context, AttributeSet attrs) {
        super(context, attrs);
        if (!isInEditMode()) {
            ComponentHolder.getActivityComponent().inject(this);
        }
    }

    @Override
    void inflateContent(LayoutInflater layoutInflater) {
        layoutInflater.inflate(R.layout.v__search, this);
    }

    @Override
    Disposable subscribeForEvents() {
        return new CompositeDisposable(
            subscribeForSearchRequests(),
            subscribeForSearchResults(),
            subscribeForResetRequests()
        );
    }

    private Disposable subscribeForSearchRequests() {
        return inputView.getText()
            .debounce(query -> {
                if (query.length() == 0) {
                    return Observable.just(0L);
                }
                return Observable.timer(SEARCH_DELAY.getMillis(), TimeUnit.MILLISECONDS);
            })
            .subscribe(presenters.getSearchPresenter().viewInput.searchQuery::set);
    }

    private Disposable subscribeForSearchResults() {
        Observable<Result<SearchPresenter.SearchResult>> common = presenters.getSearchPresenter().getSearchResults();
        return new CompositeDisposable(
            common
                .filter(Result::isLoading)
                .compose(commonFunctions.toMainThread())
                .subscribe(o -> inputView.setClearIcon(R.drawable.ic_hourglass)),
            common
                .filter(Result::isSuccess)
                .compose(commonFunctions.toMainThread())
                .subscribe(items -> inputView.resetClearIcon()),
            common
                .filter(Result::isFail)
                .compose(commonFunctions.toMainThread())
                .subscribe(o -> inputView.setClearIcon(R.drawable.ic_warning))
        );
    }

    private Disposable subscribeForResetRequests() {
        return presenters.getSearchPresenter().getResetRequests()
            .compose(commonFunctions.toMainThread())
            .subscribe(o -> inputView.clear());
    }
}
