package micdm.transportlive2.ui.views;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;

import micdm.transportlive2.ComponentHolder;
import micdm.transportlive2.R;

public class LoadingView extends BaseView {

    public LoadingView(Context context, AttributeSet attrs) {
        super(context, attrs);
        if (!isInEditMode()) {
            ComponentHolder.getActivityComponent().inject(this);
        }
    }

    @Override
    protected void inflateContent(LayoutInflater layoutInflater) {
        layoutInflater.inflate(R.layout.v__loading, this);
    }
}
