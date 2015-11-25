package com.example.temp;

import java.text.SimpleDateFormat;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.ImageView;

public class RefreshListView extends ListView implements OnScrollListener {

	private static final String TAG = "RefreshListView";
	private int firstVisibleItemPosition; // 屏幕显示在第一个的item的索引
	private int downY; 	// 按下时y轴的偏移量
	
	private int headerViewHeight, footerViewHeight; // 头布局的高度
	private View headerView, footerView; // 头布局的对象
	ImageView ivArrow;
	ProgressBar mProgressBar;
	TextView tvState, tvLastUpdateTime;
	
	RotateAnimation upAnimation, downAnimation;
	
	private final int DOWN_PULL_REFRESH = 0; 	// 下拉刷新状态
	private final int RELEASE_REFRESH = 1;		// 松开刷新
	private final int REFRESHING = 2; // 正在刷新中
	private int currentState = DOWN_PULL_REFRESH;	// 头布局的状态默认为下拉刷新状态
	private boolean isLoadingMore = false;	// 是否正在加载更多
	private boolean isScrollToBottom = false;
	private OnRefreshListener mOnRefreshListener;
	
	public RefreshListView(Context context, AttributeSet attrs) {
		super(context, attrs);
		initHeaderView();
		initFooterView();
		this.setOnScrollListener(this);
	}
	
	/**
	 * 初始化脚布局
	 */
	private void initFooterView() {
		footerView = View.inflate(getContext(), R.layout.listview_footer, null);
		footerView.measure(0, 0);
		footerViewHeight = footerView.getMeasuredHeight();
		footerView.setPadding(0, -footerViewHeight, 0, 0);
		this.addFooterView(footerView);
	}
	
	/**
	 * 初始化头布局
	 */
	private void initHeaderView() {
		headerView = View.inflate(getContext(), R.layout.listview_header, null);
		ivArrow = (ImageView) headerView.findViewById(R.id.iv_listview_header_arrow);
		mProgressBar = (ProgressBar) headerView.findViewById(R.id.pb_listview_header);
		tvState = (TextView) headerView.findViewById(R.id.tv_listview_header_state);
		tvLastUpdateTime = (TextView) headerView.findViewById(R.id.tv_listview_header_last_update_time);
		
		// 设置最后刷新时间
		tvLastUpdateTime.setText("最后刷新时间：" + getLastUpdateTime());
		
		headerView.measure(0, 0); // 系统会帮我们测量出headerview的高度
		headerViewHeight = headerView.getMeasuredHeight();
		headerView.setPadding(0, -headerViewHeight, 0, 0);
		this.addHeaderView(headerView); // 向listview顶部添加一个view对象
		initAnimation();
	}
	
	/**
	 * 初始化动画
	 */
	private void initAnimation() {
		// RotateAnimation(旋转的开始角度，旋转的结束角度，X轴的伸缩模式，X坐标的伸缩值，Y轴的伸缩模式，Y坐标的伸缩值)
		upAnimation = new RotateAnimation(0f, -180f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
		upAnimation.setDuration(500);
		upAnimation.setFillAfter(true); // 动画结束后，停留在结束的位置上
		
		downAnimation = new RotateAnimation(-180f, -360f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
		downAnimation.setDuration(500);
		downAnimation.setFillAfter(true);
	}
	
	
	/**
	 * 获得系统的最新时间
	 * @return
	 */
	private String getLastUpdateTime() {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		return sdf.format(System.currentTimeMillis());
	}
	
	@Override
	public boolean onTouchEvent(MotionEvent ev) {
		switch (ev.getAction()) {
		case MotionEvent.ACTION_DOWN:
			downY = (int) ev.getY();
			break;
		case MotionEvent.ACTION_MOVE:
			int moveY = (int) ev.getY();
			int diff = (moveY - downY) / 2;	// 间距 = 移动中的Y - 按下的Y
			int paddingTop = -headerViewHeight + diff; // -头布局的高度 + 间距
			if (firstVisibleItemPosition == 0 && -headerViewHeight < paddingTop) {
				if (paddingTop > 0 && currentState == DOWN_PULL_REFRESH) { // 完全显示了
					Log.i(TAG, "松开刷新");
					currentState = RELEASE_REFRESH;
					refreshHeaderView();
				} else if (paddingTop < 0 && currentState == RELEASE_REFRESH) {
					Log.i(TAG, "下拉刷新");
					currentState = DOWN_PULL_REFRESH;
					refreshHeaderView();
				}
				headerView.setPadding(0, paddingTop, 0, 0);
				return true;
			}
			break;
		case MotionEvent.ACTION_UP:
			// 判断当前状态是松开刷新还是下拉刷新
			if (currentState == RELEASE_REFRESH) {
				Log.i(TAG, "刷新数据");
				headerView.setPadding(0, 0, 0, 0);
				currentState = REFRESHING;
				refreshHeaderView();
				
				if (mOnRefreshListener != null) {
					mOnRefreshListener.onDownPullRefresh();
				} 
			} else if (currentState == DOWN_PULL_REFRESH) {
				headerView.setPadding(0, -headerViewHeight, 0, 0);
			}
			break;
			default:
				break;
		}
		
		return super.onTouchEvent(ev);
	}
	
	public void setOnRefreshListener(OnRefreshListener listener) {
		mOnRefreshListener = listener;
	}
	
	/**
	 * 根据currentState信息刷新头布局信息
	 */
	private void refreshHeaderView() {
		switch (currentState) {
		case DOWN_PULL_REFRESH:
			tvState.setText("下拉刷新");
			ivArrow.startAnimation(downAnimation);
			break;
		case RELEASE_REFRESH:
			tvState.setText("松开刷新");
			ivArrow.startAnimation(upAnimation);
			break;
		case REFRESHING:
			ivArrow.clearAnimation();
			ivArrow.setVisibility(View.GONE);
			mProgressBar.setVisibility(View.VISIBLE);
			tvState.setText("正在刷新中...");
			break;
		default:break;
		}
	}
	
	/**
	 * 当滚动状态改变时回调
	 * @param view
	 * @param scrollState
	 */
	@Override
	public void onScrollStateChanged(AbsListView view, int scrollState) {
		if (scrollState == SCROLL_STATE_IDLE || scrollState == SCROLL_STATE_FLING) {
			if (isScrollToBottom && !isLoadingMore) {
				isLoadingMore = true;
				footerView.setPadding(0, 0, 0, 0);
				this.setSelection(this.getCount());
				
				if (mOnRefreshListener != null) {
					mOnRefreshListener.onLoadingMore();
				}
			}
		}
	}

	/**
	 * 当滚动时调用
	 * @param view
	 * @param firstVisibleItem 当前屏幕显示在顶部的item的position
	 * @param visibleItemCount	当前屏幕显示了多少个条目的总数
	 * @param totalItemCount 	listview的总条目数
	 */
	@Override
	public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
		firstVisibleItemPosition = firstVisibleItem;
		
		if (getLastVisiblePosition() == (totalItemCount - 1)) {
			isScrollToBottom = true;
		} else {
			isScrollToBottom = false;
		}
	}

	public void hideHeaderView() {
		headerView.setPadding(0, -headerViewHeight, 0, 0);
		ivArrow.setVisibility(View.VISIBLE);
		mProgressBar.setVisibility(View.GONE);
		tvState.setText("下拉刷新");
		tvLastUpdateTime.setText("最后刷新时间：" + getLastUpdateTime());
		currentState = DOWN_PULL_REFRESH;
	}
	
	public void hideFooterView() {
		footerView.setPadding(0, -footerViewHeight, 0, 0);
		isLoadingMore = false;
	}
}
