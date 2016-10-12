package party.danyang.nationalgeographic.widget;

/**
 * Created by dream on 16-8-17.
 */
public interface OnStateChangedListener {
    //展开
    void onExpanded();

    //折叠
    void onCollapsed();

    //中间状态
    void onInternediate();
}