package micdm.transportlive.ui.views;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.jakewharton.rxbinding2.view.RxView;
import com.jakewharton.rxbinding2.widget.RxTextView;

import butterknife.BindView;
import io.reactivex.Observable;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import micdm.transportlive.ComponentHolder;
import micdm.transportlive.R;

public class ClearableEditText extends BaseView {

    @BindView(R.id.v__clearabled_edit_text__input)
    TextView inputView;
    @BindView(R.id.v__clearabled_edit_text__clear)
    View clearView;

    private final String hint;

    public ClearableEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
        if (!isInEditMode()) {
            ComponentHolder.getActivityComponent().inject(this);
        }
        TypedArray attributes = context.obtainStyledAttributes(attrs, R.styleable.ClearableEditText);
        hint = attributes.getString(R.styleable.ClearableEditText_hint);
        attributes.recycle();
    }

    @Override
    void inflateContent(LayoutInflater layoutInflater) {
        layoutInflater.inflate(R.layout.v__clearable_edit_text, this);
    }

    @Override
    void setupViews() {
        inputView.setHint(hint);
    }

    @Override
    Disposable subscribeForEvents() {
        return new CompositeDisposable(
            subscribeForInput(),
            subscribeForClear()
        );
    }

    Disposable subscribeForInput() {
        return getText()
            .map(text -> text.length() == 0)
            .subscribe(isEmpty -> clearView.setVisibility(isEmpty ? GONE : VISIBLE));
    }

    Disposable subscribeForClear() {
        return RxView.clicks(clearView).subscribe(o -> clear());
    }

    Observable<CharSequence> getText() {
        return RxTextView.textChanges(inputView);
    }

    void clear() {
        inputView.setText("");
    }
}
