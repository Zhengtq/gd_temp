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
	private int firstVisibleItemPosition; // ��Ļ��ʾ�ڵ�һ����item������
	private int downY, downX, curX, curY; 	// ����ʱy���ƫ����
	private int itemHeight = 50;
	private int ratio = 1;
	
	private int headerViewHeight, footerViewHeight; // ͷ���ֵĸ߶�
	private View headerView, footerView; // ͷ���ֵĶ���
	ImageView ivArrow;
	ProgressBar mProgressBar;
	TextView tvState, tvLastUpdateTime;
	
	RotateAnimation upAnimation, downAnimation;
	
	private final int DOWN_PULL_REFRESH = 0; 	// ����ˢ��״̬
	private final int RELEASE_REFRESH = 1;		// �ɿ�ˢ��
	private final int REFRESHING = 2; // ����ˢ����
	private int currentState = DOWN_PULL_REFRESH;	// ͷ���ֵ�״̬Ĭ��Ϊ����ˢ��״̬
	private boolean isLoadingMore = false;	// �Ƿ����ڼ��ظ���
	private boolean isScrollToBottom = false;
	private boolean deleteFlag = false;	// �Ƿ�ɾ��
	private OnFlipperDeleteListener mOnFlipperDeleteListener;
	
	public FlipperListView(Context context, AttributeSet attrs) {
		super(context, attrs);
		
		initHeaderView();
		initFooterView();
		this.setOnScrollListener(this);
	}
	
	/**
	 * ��ʼ���Ų���
	 */
	private void initFooterView() {
		footerView = View.inflate(getContext(), R.layout.listview_footer, null);
		footerView.measure(0, 0);
		footerViewHeight = footerView.getMeasuredHeight();
		footerView.setPadding(0, -footerViewHeight, 0, 0);
		this.addFooterView(footerView);
	}
	
	/**
	 * ��ʼ��ͷ����
	 */
	private void initHeaderView() {
		headerView = View.inflate(getContext(), R.layout.listview_header, null);
		ivArrow = (ImageView) headerView.findViewById(R.id.iv_listview_header_arrow);
		mProgressBar = (ProgressBar) headerView.findViewById(R.id.pb_listview_header);
		tvState = (TextView) headerView.findViewById(R.id.tv_listview_header_state);
		tvLastUpdateTime = (TextView) headerView.findViewById(R.id.tv_listview_header_last_update_time);
		
		// �������ˢ��ʱ��
		tvLastUpdateTime.setText("���ˢ��ʱ�䣺" + getLastUpdateTime());
		
		headerView.measure(0, 0); // ϵͳ������ǲ�����headerview�ĸ߶�
		headerViewHeight = headerView.getMeasuredHeight();
		headerView.setPadding(0, -headerViewHeight, 0, 0);
		this.addHeaderView(headerView); // ��listview�������һ��view����
		initAnimation();
	}
	
	/**
	 * ��ʼ������
	 */
	private void initAnimation() {
		// RotateAnimation(��ת�Ŀ�ʼ�Ƕȣ���ת�Ľ����Ƕȣ�X�������ģʽ��X���������ֵ��Y�������ģʽ��Y���������ֵ)
		upAnimation = new RotateAnimation(0f, -180f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
		upAnimation.setDuration(500);
		upAnimation.setFillAfter(true); // ����������ͣ���ڽ�����λ����
		
		downAnimation = new RotateAnimation(-180f, -360f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
		downAnimation.setDuration(500);
		downAnimation.setFillAfter(true);
	}
	
	
	/**
	 * ���ϵͳ������ʱ��
	 * @return
	 */
	private String getLastUpdateTime() {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		return sdf.format(System.currentTimeMillis());
	}

	@Override
	public boolean onTouchEvent(MotionEvent ev) {
		switch (ev.getAction()) {
		// ���°���˲������꣬��Ҫ�ǳ�ʼ���������ݣ������Ժ�����ƶ��ľ���
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
			
		// �ƶ�������̬λ��������ƶ�����
		case MotionEvent.ACTION_MOVE:
			float deltaX = ev.getX(ev.getPointerCount() - 1) - downX;
			int moveY = (int) ev.getY();
			int diffY = (moveY - downY);	// ��� = �ƶ��е�Y - ���µ�Y
			int moveX = (int) ev.getX();
			int diffX = (moveX - downX);
			curX = moveX; curY = moveY;
			int paddingTop = -headerViewHeight + diffY; // -ͷ���ֵĸ߶� + ���
			if (firstVisibleItemPosition == 0 && 0 < paddingTop && Math.abs(diffX) < this.getWidth() / ratio) { // -headerViewHeight
				if (paddingTop > 0 && currentState == DOWN_PULL_REFRESH) { // ��ȫ��ʾ��
					Log.i(TAG, "�ɿ�ˢ��");
					currentState = RELEASE_REFRESH;
					refreshHeaderView();
				} else if (paddingTop < 0 && currentState == RELEASE_REFRESH) {
					Log.i(TAG, "����ˢ��");
					currentState = DOWN_PULL_REFRESH;
					refreshHeaderView();
				}
				headerView.setPadding(0, paddingTop, 0, 0);
				return true;
			}
			if (-20 < diffY && diffY < 20) {	// ������ָ�����ƶ����벻��̫��ֻˮƽ�ƶ� diffX < 0 && 
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
		
		// ��Ҫ���ƶ�����֮���ж��ƶ�λ���ǻ�ԭ��λ�ã������ƶ���ɾ����λ��
		case MotionEvent.ACTION_UP:
			// �жϵ�ǰ״̬���ɿ�ˢ�»�������ˢ��
			if (currentState == RELEASE_REFRESH) {
				Log.i(TAG, "ˢ������");
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
	 * ����currentState��Ϣˢ��ͷ������Ϣ
	 */
	private void refreshHeaderView() {
		switch (currentState) {
		case DOWN_PULL_REFRESH:
			tvState.setText("����ˢ��");
			ivArrow.startAnimation(downAnimation);
			break;
		case RELEASE_REFRESH:
			tvState.setText("�ɿ�ˢ��");
			ivArrow.startAnimation(upAnimation);
			break;
		case REFRESHING:
			ivArrow.clearAnimation();
			ivArrow.setVisibility(View.GONE);
			mProgressBar.setVisibility(View.VISIBLE);
			tvState.setText("����ˢ����...");
			break;
		default:break;
		}
	}
	
	/**
	 * ������״̬�ı�ʱ�ص�
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
	 * ������ʱ����
	 * @param view
	 * @param firstVisibleItem ��ǰ��Ļ��ʾ�ڶ�����item��position
	 * @param visibleItemCount	��ǰ��Ļ��ʾ�˶��ٸ���Ŀ������
	 * @param totalItemCount 	listview������Ŀ��
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
		tvState.setText("����ˢ��");
		tvLastUpdateTime.setText("���ˢ��ʱ�䣺" + getLastUpdateTime());
		currentState = DOWN_PULL_REFRESH;
	}
	
	public void hideFooterView() {
		footerView.setPadding(0, -footerViewHeight, 0, 0);
		isLoadingMore = false;
	}
}
