package party.danyang.nationalgeographic.widget;

/**
 * Created by dream on 16-8-17.
 */
public interface OnStateChangedListener {
    //展开
    void onExpanded();

    //折叠
    void onCollapsed();

    //展开向折叠时的中间状态
    void onInternediateFromExpand();

    //折叠向展开时的中间状态
    void onInternediateFromCollapsed();

    //中间状态
    void onInternediate();
}