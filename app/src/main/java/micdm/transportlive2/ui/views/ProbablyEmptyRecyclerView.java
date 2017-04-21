package micdm.transportlive2.ui.views;

import android.content.Context;
import android.content.res.TypedArray;
import android.support.annotation.LayoutRes;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.TextView;

import javax.inject.Inject;

import butterknife.BindView;
import micdm.transportlive2.ComponentHolder;
import micdm.transportlive2.R;

public class ProbablyEmptyRecyclerView extends BaseView {

    @Inject
    LayoutInflater layoutInflater;

    @BindView(R.id.v__probably_empty_recycler_view__content)
    ViewGroup contentView;
    @BindView(R.id.v__probably_empty_recycler_view__list)
    RecyclerView listView;
    TextView messageView;

    private final RecyclerView.AdapterDataObserver dataObserver = new RecyclerView.AdapterDataObserver() {
        @Override
        public void onChanged() {
            if (listView.getAdapter().getItemCount() == 0 && isEmptyCheckEnabled) {
                listView.setVisibility(GONE);
                messageView.setVisibility(VISIBLE);
            } else {
                listView.setVisibility(VISIBLE);
                messageView.setVisibility(GONE);
            }
        }
    };
    @LayoutRes
    private final int messageLayoutId;
    private boolean isEmptyCheckEnabled = true;

    public ProbablyEmptyRecyclerView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        if (!isInEditMode()) {
            ComponentHolder.getActivityComponent().inject(this);
        }
        TypedArray attributes = context.obtainStyledAttributes(attrs, R.styleable.ProbablyEmptyRecyclerView);
        messageLayoutId = attributes.getResourceId(R.styleable.ProbablyEmptyRecyclerView_message, 0);
        if (messageLayoutId == 0) {
            throw new IllegalStateException("message layout ID must be set");
        }
        attributes.recycle();
    }

    @Override
    void inflateContent(LayoutInflater layoutInflater) {
        layoutInflater.inflate(R.layout.v__probably_empty_recycler_view, this);
    }

    @Override
    void setupViews() {
        listView.setVisibility(GONE);
        messageView = (TextView) layoutInflater.inflate(messageLayoutId, contentView, false);
        messageView.setVisibility(GONE);
        contentView.addView(messageView);
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

    public void setItemAnimator(RecyclerView.ItemAnimator animator) {
        listView.setItemAnimator(animator);
    }

    public void setEmptyCheckEnabled(boolean isEnabled) {
        isEmptyCheckEnabled = isEnabled;
        dataObserver.onChanged();
    }
}
