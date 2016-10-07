package party.danyang.nationalgeographic.adapter.base;

import android.databinding.DataBindingUtil;
import android.databinding.ViewDataBinding;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.jakewharton.rxbinding.view.RxView;

import java.util.List;

import rx.functions.Action1;

/**
 * Created by dream on 16-7-27.
 */
public abstract class BaseAdapter<E> extends ArrayRecyclerAdapter<E, BaseAdapter.ViewHolder> {

    private int layoutRes;

    public BaseAdapter(int layoutRes, List<E> data) {
        super(data);
        if (layoutRes != 0) {
            this.layoutRes = layoutRes;
        }
    }

    public BaseAdapter(int layoutResId) {
        this(layoutResId, null);
    }

    public BaseAdapter() {
        this(0);
    }

    public void setLayoutRes(int layoutRes) {
        this.layoutRes = layoutRes;
    }

    public abstract void setBingVariables(ViewDataBinding binding, int position);

    @Override
    public void onBindViewHolder(final BaseAdapter.ViewHolder holder, final int position) {
        setBingVariables(holder.getBinding(), position);
        if (mOnItemClickListener != null)
            RxView.clicks(holder.itemView).subscribe(new Action1<Void>() {
                @Override
                public void call(Void aVoid) {
                    mOnItemClickListener.onItemClick(holder.itemView, position);
                }
            });
        holder.getBinding().executePendingBindings();
    }

    @Override
    public BaseAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        ViewDataBinding binding = DataBindingUtil.inflate(LayoutInflater
                .from(parent.getContext()), layoutRes, parent, false);
        ViewHolder holder = new ViewHolder(binding.getRoot());
        holder.setBinding(binding);
        return holder;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        private ViewDataBinding binding;

        public ViewHolder(View itemView) {
            super(itemView);
        }

        public void setBinding(ViewDataBinding binding) {
            this.binding = binding;
        }

        public ViewDataBinding getBinding() {
            return this.binding;
        }
    }

    public interface OnItemClickListener {
        void onItemClick(View view, int position);
    }

    private OnItemClickListener mOnItemClickListener;

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.mOnItemClickListener = onItemClickListener;
    }
}
