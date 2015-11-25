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
import android.widget.ImageView;
import android.widget.AbsListView.OnScrollListener;

public class FlipperListView extends ListView implements OnScrollListener {

	private static final String TAG = "RefreshListView";
	private int firstVisibleItemPosition; // 屏幕显示在第一个的item的索引
	private int downY, downX, curX, curY; 	// 按下时y轴的偏移量
	private int itemHeight = 50;
	private int ratio = 1;
	
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
	private boolean deleteFlag = false;	// 是否删除
	private OnFlipperDeleteListener mOnFlipperDeleteListener;
	
	public FlipperListView(Context context, AttributeSet attrs) {
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
		// 记下按下瞬间的坐标，主要是初始化坐标数据，方便以后计算移动的距离
		case MotionEvent.ACTION_DOWN:
			int temp = getChildCount();
			itemHeight = (temp == 0) ? itemHeight : getChildAt(0).getHeight();
			downY = (int) ev.getY();
			downX = (int) ev.getX();
			curX = downX;
			curY = downY;
			if (mOnFlipperDeleteListener != null) {
				mOnFlipperDeleteListener.getMoveY(curX, curY);
			}
			System.out.println("action down!");
			break;
			
		// 移动鉴定动态位置坐标的移动处理
		case MotionEvent.ACTION_MOVE:
			float deltaX = ev.getX(ev.getPointerCount() - 1) - downX;
			int moveY = (int) ev.getY();
			int diffY = (moveY - downY);	// 间距 = 移动中的Y - 按下的Y
			int moveX = (int) ev.getX();
			int diffX = (moveX - downX);
			curX = moveX; curY = moveY;
			int paddingTop = -headerViewHeight + diffY; // -头布局的高度 + 间距
			if (firstVisibleItemPosition == 0 && 0 < paddingTop && Math.abs(diffX) < this.getWidth() / ratio) { // -headerViewHeight
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
			if (-20 < diffY && diffY < 20) {	// 限制手指上下移动距离不能太大，只水平移动 diffX < 0 && 
				if (itemHeight > diffY && mOnFlipperDeleteListener != null) {
					mOnFlipperDeleteListener.onFlipping(downX, downY, diffX, diffY);
				}
				if (Math.abs(diffX) > this.getWidth() / ratio) {
					deleteFlag = true;
				} else {
					deleteFlag = false;
				}
			}
			System.out.println("action move!");
			break;
		
		// 主要是移动距离之后判断移动位置是回原来位置，还是移动到删除的位置
		case MotionEvent.ACTION_UP:
			// 判断当前状态是松开刷新还是下拉刷新
			if (currentState == RELEASE_REFRESH) {
				Log.i(TAG, "刷新数据");
				headerView.setPadding(0, 0, 0, 0);
				currentState = REFRESHING;
				refreshHeaderView();
				
				if (mOnFlipperDeleteListener != null) {
					mOnFlipperDeleteListener.onDownPullRefresh();
				} 
			} else if (currentState == DOWN_PULL_REFRESH) {
				headerView.setPadding(0, -headerViewHeight, 0, 0);
				
				if (deleteFlag && mOnFlipperDeleteListener != null) {
					mOnFlipperDeleteListener.restoreView(curX, curY, true);
				}
				if (!deleteFlag && mOnFlipperDeleteListener != null) {
					mOnFlipperDeleteListener.restoreView(downX, downY, false);
				}
				reset();
			}
			System.out.println("action up!");
			break;
			default:
				break;
		}
		
		return super.onTouchEvent(ev);
	}
	
	public void reset() {
		deleteFlag = false;
		downX = -1;
		downY = -1;
	}
	
	public void setOnFlipperDeleteListener(OnFlipperDeleteListener listener) {
		mOnFlipperDeleteListener = listener;
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
				
				if (mOnFlipperDeleteListener != null) {
					mOnFlipperDeleteListener.onLoadingMore();
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
