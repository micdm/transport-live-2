package micdm.transportlive.ui;

import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.bluelinelabs.conductor.Controller;

import butterknife.ButterKnife;
import butterknife.Unbinder;
import io.reactivex.Observable;
import io.reactivex.disposables.Disposable;
import io.reactivex.subjects.PublishSubject;
import io.reactivex.subjects.Subject;
import micdm.transportlive.misc.Irrelevant;

abstract class BaseController extends Controller implements BasePresenter.View {

    private final Subject<Object> attaches = PublishSubject.create();
    private final Subject<Object> detaches = PublishSubject.create();

    private Unbinder viewUnbinder;
    private Disposable eventSubscription;

    @NonNull
    @Override
    protected View onCreateView(@NonNull LayoutInflater inflater, @NonNull ViewGroup container) {
        View view = inflateContent(inflater, container);
        viewUnbinder = ButterKnife.bind(this, view);
        initRouter();
        setupViews();
        return view;
    }

    @NonNull
    abstract View inflateContent(@NonNull LayoutInflater inflater, @NonNull ViewGroup container);

    protected void initRouter() {

    }

    protected void setupViews() {

    }

    @Override
    protected void onAttach(@NonNull View view) {
        eventSubscription = subscribeForEvents();
        attaches.onNext(Irrelevant.INSTANCE);
    }

    protected Disposable subscribeForEvents() {
        return null;
    }

    @Override
    protected void onDetach(@NonNull View view) {
        detaches.onNext(Irrelevant.INSTANCE);
        if (eventSubscription != null) {
            eventSubscription.dispose();
        }
    }

    @Override
    protected void onDestroyView(@NonNull View view) {
        if (viewUnbinder != null) {
            viewUnbinder.unbind();
        }
    }

    @Override
    public Observable<Object> getAttaches() {
        return attaches;
    }

    @Override
    public Observable<Object> getDetaches() {
        return detaches;
    }
}
