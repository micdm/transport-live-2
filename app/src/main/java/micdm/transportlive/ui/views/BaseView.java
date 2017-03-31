package micdm.transportlive.ui.views;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.FrameLayout;

import javax.inject.Inject;

import butterknife.ButterKnife;
import io.reactivex.disposables.Disposable;

public abstract class BaseView extends FrameLayout {

    @Inject
    LayoutInflater layoutInflater;

    private Disposable eventSubscription;

    public BaseView(Context context) {
        super(context);
        createViewHierarchy();
    }

    public BaseView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        createViewHierarchy();
    }

    private void createViewHierarchy() {
        inflateContent(layoutInflater);
        if (!isInEditMode()) {
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
            eventSubscription = subscribeForEvents();
        }
    }

    Disposable subscribeForEvents() {
        return null;
    }

    @Override
    protected void onDetachedFromWindow() {
        if (eventSubscription != null) {
            eventSubscription.dispose();
        }
        cleanup();
        super.onDetachedFromWindow();
    }

    void cleanup() {}
}
