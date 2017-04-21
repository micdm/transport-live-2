package micdm.transportlive2.ui.views;

import android.content.Context;
import android.content.res.TypedArray;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.TextView;

import butterknife.BindView;
import micdm.transportlive2.ComponentHolder;
import micdm.transportlive2.R;

public class ProbablyEmptyRecyclerView extends BaseView {

    @BindView(R.id.v__probably_empty_recycler_view__list)
    RecyclerView listView;
    @BindView(R.id.v__probably_empty_recycler_view__message)
    TextView messageView;

    private final RecyclerView.AdapterDataObserver dataObserver = new RecyclerView.AdapterDataObserver() {
        @Override
        public void onChanged() {
            if (listView.getAdapter().getItemCount() == 0) {
                listView.setVisibility(GONE);
                messageView.setVisibility(VISIBLE);
            } else {
                listView.setVisibility(VISIBLE);
                messageView.setVisibility(GONE);
            }
        }
    };
    private final CharSequence text;

    public ProbablyEmptyRecyclerView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        if (!isInEditMode()) {
            ComponentHolder.getActivityComponent().inject(this);
        }
        TypedArray attributes = context.obtainStyledAttributes(attrs, R.styleable.ProbablyEmptyRecyclerView);
        text = attributes.getString(R.styleable.ProbablyEmptyRecyclerView_text);
        attributes.recycle();
    }

    @Override
    void inflateContent(LayoutInflater layoutInflater) {
        layoutInflater.inflate(R.layout.v__probably_empty_recycler_view, this);
    }

    @Override
    void setupViews() {
        listView.setVisibility(GONE);
        messageView.setText(text);
        messageView.setVisibility(GONE);
    }

    public void setLayoutManager(RecyclerView.LayoutManager layoutManager) {
        listView.setLayoutManager(layoutManager);
    }

    public void setAdapter(RecyclerView.Adapter adapter) {
        if (listView.getAdapter() != null) {
            listView.getAdapter().unregisterAdapterDataObserver(dataObserver);
        }
        adapter.registerAdapterDataObserver(dataObserver);
        listView.setAdapter(adapter);
    }

    public RecyclerView.Adapter getAdapter() {
        return listView.getAdapter();
    }
}
