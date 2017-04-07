package micdm.transportlive2.ui.views;

import android.content.Context;
import android.content.res.Resources;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.jakewharton.rxbinding2.view.RxView;

import javax.inject.Inject;

import butterknife.BindView;
import io.reactivex.Observable;
import micdm.transportlive2.BuildConfig;
import micdm.transportlive2.ComponentHolder;
import micdm.transportlive2.R;

public class AboutView extends BaseView {

    @Inject
    Resources resources;

    @BindView(R.id.v__about__title)
    TextView titleView;
    @BindView(R.id.v__about__ok)
    View okView;

    public AboutView(Context context, AttributeSet attrs) {
        super(context, attrs);
        if (!isInEditMode()) {
            ComponentHolder.getActivityComponent().inject(this);
        }
    }

    @Override
    void inflateContent(LayoutInflater layoutInflater) {
        layoutInflater.inflate(R.layout.v__about, this);
    }

    @Override
    void setupViews() {
        titleView.setText(resources.getString(R.string.v__about__title, resources.getString(R.string.app_name), BuildConfig.VERSION_NAME));
    }

    public Observable<Object> getCloseRequests() {
        return RxView.clicks(okView);
    }
}
