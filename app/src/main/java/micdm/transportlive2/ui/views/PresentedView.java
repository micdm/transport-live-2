package micdm.transportlive2.ui.views;

import android.content.Context;
import android.util.AttributeSet;

public abstract class PresentedView extends BaseView {

    public PresentedView(Context context) {
        super(context);
    }

    public PresentedView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    void onAttach() {
        attachToPresenters();
    }

    abstract void attachToPresenters();

    @Override
    void onDetach() {
        detachFromPresenters();
    }

    abstract void detachFromPresenters();
}
