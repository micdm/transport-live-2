package micdm.transportlive2.ui.views;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.FrameLayout;

import javax.inject.Inject;

import butterknife.ButterKnife;
import io.reactivex.disposables.Disposable;

abstract class BaseView extends FrameLayout {

    @Inject
    LayoutInflater layoutInflater;

    private Disposable subscription;

    public BaseView(Context context) {
        super(context);
    }

    public BaseView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        createViewHierarchy();
    }

    void createViewHierarchy() {
        if (!isInEditMode()) {
            inflateContent(layoutInflater);
            ButterKnife.bind(this);
            setupViews();
        }
    }

    void inflateContent(LayoutInflater layoutInflater) {}

    void setupViews() {}

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (!isInEditMode()) {
            subscription = subscribeForEvents();
            onAttach();
        }
    }

    Disposable subscribeForEvents() {
        return null;
    }

    void onAttach() {

    }

    @Override
    protected void onDetachedFromWindow() {
        onDetach();
        if (subscription != null) {
            subscription.dispose();
        }
        super.onDetachedFromWindow();
    }

    void onDetach() {

    }
}
