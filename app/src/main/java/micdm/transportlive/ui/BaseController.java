package micdm.transportlive.ui;

import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.bluelinelabs.conductor.Controller;

import butterknife.ButterKnife;
import butterknife.Unbinder;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.subjects.PublishSubject;
import io.reactivex.subjects.Subject;
import micdm.transportlive.misc.Irrelevant;

abstract class BaseController extends Controller {

    private final Subject<Object> attaches = PublishSubject.create();
    private final Subject<Object> detaches = PublishSubject.create();

    private Unbinder viewUnbinder;
    private CompositeDisposable subscription;

    @NonNull
    @Override
    protected View onCreateView(@NonNull LayoutInflater inflater, @NonNull ViewGroup container) {
        View view = inflateContent(inflater, container);
        viewUnbinder = ButterKnife.bind(this, view);
        setupViews();
        return view;
    }

    @NonNull
    abstract View inflateContent(@NonNull LayoutInflater inflater, @NonNull ViewGroup container);

    protected void setupViews() {

    }

    @Override
    protected void onAttach(@NonNull View view) {
        subscription = new CompositeDisposable(
            subscribeForAttach(),
            subscribeForDetach()
        );
        Disposable eventSubscription = subscribeForEvents();
        if (eventSubscription != null) {
            subscription.add(eventSubscription);
        }
        attaches.onNext(Irrelevant.INSTANCE);
    }

    private Disposable subscribeForAttach() {
        return attaches.subscribe(o -> attachToPresenters());
    }

    void attachToPresenters() {

    }

    private Disposable subscribeForDetach() {
        return detaches.subscribe(o -> detachFromPresenters());
    }

    void detachFromPresenters() {

    }

    protected Disposable subscribeForEvents() {
        return null;
    }

    @Override
    protected void onDetach(@NonNull View view) {
        detaches.onNext(Irrelevant.INSTANCE);
        if (subscription != null) {
            subscription.dispose();
        }
    }

    @Override
    protected void onDestroyView(@NonNull View view) {
        if (viewUnbinder != null) {
            viewUnbinder.unbind();
        }
    }
}
